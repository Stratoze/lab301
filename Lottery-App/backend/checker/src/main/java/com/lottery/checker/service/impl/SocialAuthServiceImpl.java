package com.lottery.checker.service.impl;

import com.lottery.checker.dto.request.SocialLoginRequest;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.service.UserCodeGenerator;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.entity.UserAuthProvider;
import com.lottery.checker.repository.UserAuthProviderRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.security.JwtService;
import com.lottery.checker.service.SocialAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SocialAuthServiceImpl implements SocialAuthService {

    public SocialAuthServiceImpl(UserRepository userRepository,
                                  UserAuthProviderRepository authProviderRepository,
                                  JwtService jwtService,
                                  UserCodeGenerator userCodeGenerator) {
        this.userRepository = userRepository;
        this.authProviderRepository = authProviderRepository;
        this.jwtService = jwtService;
        this.userCodeGenerator = userCodeGenerator;

        // RestTemplate with connect/read timeouts to prevent thread blocking
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(5));
        factory.setReadTimeout(java.time.Duration.ofSeconds(10));
        this.restTemplate = new RestTemplate(factory);
    }

    private final UserRepository userRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final UserCodeGenerator userCodeGenerator;

    /** JWKS cache with TTL (refresh every 6 hours) */
    private static final long JWKS_TTL_MS = 6 * 60 * 60 * 1000L;
    private volatile long jwksCacheTimestamp = 0;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${facebook.app-id:}")
    private String facebookAppId;

    @Value("${facebook.app-secret:}")
    private String facebookAppSecret;

    /** Cached Google JWKS keys, refreshed on first failure */
    private Map<String, PublicKey> cachedGoogleKeys;

    @Override
    @Transactional
    public AuthResponse authenticate(SocialLoginRequest request) {
        SocialUserInfo info = switch (request.provider().toUpperCase()) {
            case "GOOGLE" -> verifyGoogleToken(request.token());
            case "FACEBOOK" -> verifyFacebookToken(request.token());
            default -> throw new SecurityException("Unsupported provider: " + request.provider());
        };

        User user = authProviderRepository
                .findByProviderAndProviderId(request.provider().toUpperCase(), info.providerId())
                .map(UserAuthProvider::getUser)
                .orElse(null);

        // Block inactive users from social login
        if (user != null && !user.getIsActive()) {
            throw new SecurityException("Your account is blocked. Please contact support.");
        }

        if (user == null && info.email() != null) {
            user = userRepository.findByEmail(info.email()).orElse(null);
        }

        if (user == null) {
            user = User.builder()
                    .userCode(userCodeGenerator.generate())
                    .email(info.email() != null ? info.email() : info.providerId() + "@" + request.provider().toLowerCase() + ".social")
                    .fullName(info.name() != null ? info.name() : "Social User")
                    .password(null)
                    .role(Role.ROLE_USER)
                    .isActive(true)
                    .lastLogin(LocalDateTime.now())
                    .build();
        } else {
            user.setLastLogin(LocalDateTime.now());
        }

        user = userRepository.save(user);

        boolean alreadyLinked = authProviderRepository
                .findByProviderAndProviderId(request.provider().toUpperCase(), info.providerId())
                .isPresent();
        if (!alreadyLinked) {
            authProviderRepository.save(UserAuthProvider.builder()
                    .user(user)
                    .provider(request.provider().toUpperCase())
                    .providerId(info.providerId())
                    .build());
        }

        // Generate JWT
        String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole().name()));
        return new AuthResponse(token, user.getUserCode(), user.getFullName(), user.getRole());
    }

    // ── GOOGLE: proper JWT signature + aud/iss validation ──

    private SocialUserInfo verifyGoogleToken(String idToken) {
        // 1. Decode header to get key ID (kid)
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) throw new SecurityException("Invalid token format");
        String decodedHeader = new String(Base64.getUrlDecoder().decode(parts[0]));
        String kid = extractJsonValue(decodedHeader, "kid");
        if (kid == null) throw new SecurityException("Missing kid in token header");

        // 2. Get Google public key matching kid (cached)
        PublicKey publicKey = getGooglePublicKey(kid);

        // 3. Parse and validate token signature + claims
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
        } catch (Exception e) {
            throw new SecurityException("Google token signature invalid", e);
        }

        // 4. Validate audience (must match our client ID ? handles single string or array)
        Object audObj = claims.get("aud");
        if (googleClientId != null && !googleClientId.isEmpty()) {
            boolean audMatch = false;
            if (audObj instanceof String s) {
                audMatch = googleClientId.equals(s);
            } else if (audObj instanceof java.util.Collection<?> c) {
                audMatch = c.contains(googleClientId);
            }
            if (!audMatch) {
                throw new SecurityException("Token audience does not match our client ID");
            }
        }

        // 5. Validate issuer
        String iss = claims.getIssuer();
        if (iss != null && !iss.equals("https://accounts.google.com") && !iss.equals("accounts.google.com")) {
            throw new SecurityException("Invalid issuer: " + iss);
        }

        return new SocialUserInfo(
            claims.getSubject(),
            claims.get("email", String.class),
            claims.get("name", String.class)
        );
    }

    @SuppressWarnings("unchecked")
    private PublicKey getGooglePublicKey(String kid) {
        // Use cache if available and not expired (TTL = 6 hours)
        if (cachedGoogleKeys != null
                && cachedGoogleKeys.containsKey(kid)
                && (System.currentTimeMillis() - jwksCacheTimestamp) < JWKS_TTL_MS) {
            return cachedGoogleKeys.get(kid);
        }

        // Fetch fresh JWKS
        Map<String, Object> jwks;
        try {
            jwks = restTemplate.getForObject("https://www.googleapis.com/oauth2/v3/certs", Map.class);
        } catch (Exception e) {
            throw new SecurityException("Failed to fetch Google JWKS", e);
        }
        if (jwks == null) {
            throw new SecurityException("Empty response from Google JWKS endpoint");
        }

        cachedGoogleKeys = new ConcurrentHashMap<>();
        jwksCacheTimestamp = System.currentTimeMillis();
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        for (Map<String, Object> key : keys) {
            String k = (String) key.get("kid");
            try {
                BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode((String) key.get("n")));
                BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode((String) key.get("e")));
                PublicKey pk = KeyFactory.getInstance("RSA")
                        .generatePublic(new RSAPublicKeySpec(modulus, exponent));
                cachedGoogleKeys.put(k, pk);
            } catch (Exception e) {
                throw new SecurityException("Failed to build Google public key", e);
            }
        }

        if (!cachedGoogleKeys.containsKey(kid)) {
            throw new SecurityException("No Google public key found for kid: " + kid);
        }
        return cachedGoogleKeys.get(kid);
    }

    /** Extremely simple JSON string value extractor (no extra deps needed) */
    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        if (colon < 0) return null;
        int start = json.indexOf("\"", colon);
        if (start < 0) return null;
        int end = json.indexOf("\"", start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    // ── FACEBOOK: debug_token with app access token ──

    @SuppressWarnings("unchecked")
    private SocialUserInfo verifyFacebookToken(String userAccessToken) {
        if (facebookAppId == null || facebookAppId.isEmpty() || facebookAppSecret == null || facebookAppSecret.isEmpty()) {
            throw new SecurityException("Facebook app ID or secret not configured on server");
        }

        // 1. Build app access token
        String appAccessToken = facebookAppId + "|" + facebookAppSecret;

        // 2. Debug user token
        String debugUrl = "https://graph.facebook.com/debug_token"
                + "?input_token=" + userAccessToken
                + "&access_token=" + appAccessToken;

        Map<String, Object> debugJson;
        try {
            debugJson = restTemplate.getForObject(debugUrl, Map.class);
        } catch (Exception e) {
            throw new SecurityException("Failed to verify Facebook token", e);
        }

        if (debugJson == null) throw new SecurityException("Empty response from Facebook debug_token");

        Map<String, Object> data = (Map<String, Object>) debugJson.get("data");
        if (data == null) throw new SecurityException("Missing data in Facebook debug response");

        Boolean isValid = (Boolean) data.get("is_valid");
        String appId = (String) data.get("app_id");

        if (!Boolean.TRUE.equals(isValid)) throw new SecurityException("Facebook token is not valid");
        if (!facebookAppId.equals(appId)) throw new SecurityException("Facebook token was not issued for our app");

        // 3. Get user info
        String meUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + userAccessToken;
        Map<String, Object> meJson;
        try {
            meJson = restTemplate.getForObject(meUrl, Map.class);
        } catch (Exception e) {
            throw new SecurityException("Failed to fetch Facebook user info", e);
        }

        if (meJson == null || meJson.containsKey("error")) throw new SecurityException("Invalid Facebook user token");
        return new SocialUserInfo(
            (String) meJson.get("id"),
            (String) meJson.get("email"),
            (String) meJson.get("name")
        );
    }

    // ── LINK PROVIDER (for existing users to add a social login) ──

    @Override
    @Transactional
    public void linkProvider(User user, String provider, String token) {
        SocialUserInfo info = switch (provider.toUpperCase()) {
            case "GOOGLE" -> verifyGoogleToken(token);
            case "FACEBOOK" -> verifyFacebookToken(token);
            default -> throw new SecurityException("Unsupported provider: " + provider);
        };

        // Check that this provider account is not already linked to another user
        authProviderRepository
                .findByProviderAndProviderId(provider.toUpperCase(), info.providerId())
                .ifPresent(existing -> {
                    if (!existing.getUser().getId().equals(user.getId())) {
                        throw new SecurityException("This " + provider + " account is already linked to a different user.");
                    }
                });

        authProviderRepository.save(UserAuthProvider.builder()
                .user(user)
                .provider(provider.toUpperCase())
                .providerId(info.providerId())
                .build());
    }

    private record SocialUserInfo(String providerId, String email, String name) {}
}