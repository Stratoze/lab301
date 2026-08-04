package com.lottery.checker.service;

import com.lottery.checker.dto.request.SocialLoginRequest;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.entity.UserAuthProvider;
import com.lottery.checker.repository.UserAuthProviderRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.security.JwtService;
import com.lottery.checker.service.impl.SocialAuthServiceImpl;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthProviderRepository authProviderRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserCodeGenerator userCodeGenerator;

    private SocialAuthServiceImpl socialAuthService;

    private KeyPair rsaKeyPair;

    @BeforeEach
    void setUp() throws Exception {
        socialAuthService = new SocialAuthServiceImpl(
                userRepository, authProviderRepository, jwtService, userCodeGenerator,
                "test-google-client-id", "test-fb-app-id", "test-fb-app-secret"
        );

        // Swap the internally-created RestTemplate with our mock
        ReflectionTestUtils.setField(socialAuthService, "restTemplate", restTemplate);

        // Generate RSA keypair for signing fake Google JWTs
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        rsaKeyPair = kpg.generateKeyPair();
    }

    // ─── Helpers ───

    private Map<String, Object> buildJwksResponse() {
        RSAPublicKey pub = (RSAPublicKey) rsaKeyPair.getPublic();
        String n = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(toUnsignedBytes(pub.getModulus()));
        String e = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(toUnsignedBytes(pub.getPublicExponent()));

        Map<String, Object> key = new HashMap<>();
        key.put("kid", "test-kid-1");
        key.put("kty", "RSA");
        key.put("alg", "RS256");
        key.put("use", "sig");
        key.put("n", n);
        key.put("e", e);

        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", List.of(key));
        return jwks;
    }

    private byte[] toUnsignedBytes(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();
        // Strip leading zero byte if present (sign byte)
        if (bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }

    private String createSignedGoogleJwt(String email, String name) {
        return Jwts.builder()
                .header().keyId("test-kid-1").and()
                .subject("google-uid-12345")
                .claim("email", email)
                .claim("name", name)
                .claim("aud", "test-google-client-id")
                .issuer("https://accounts.google.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(rsaKeyPair.getPrivate())
                .compact();
    }

    private void mockFacebookResponses(String fbId, String email, String name) {
        // Mock debug_token response
        Map<String, Object> debugData = new HashMap<>();
        debugData.put("is_valid", true);
        debugData.put("app_id", "test-fb-app-id");
        Map<String, Object> debugResponse = Map.of("data", debugData);

        when(restTemplate.getForObject(
                contains("debug_token"), eq(Map.class)
        )).thenReturn(debugResponse);

        // Mock /me response
        Map<String, Object> meResponse = new HashMap<>();
        meResponse.put("id", fbId);
        meResponse.put("email", email);
        meResponse.put("name", name);

        when(restTemplate.getForObject(
                contains("graph.facebook.com/me"), eq(Map.class)
        )).thenReturn(meResponse);
    }

    // ─── Google OAuth Tests ───

    @Test
    void authenticate_Google_NewUser_CreatesAccountAndLinks() {
        String idToken = createSignedGoogleJwt("newuser@gmail.com", "New Google User");
        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        when(authProviderRepository.findByProviderAndProviderId("GOOGLE", "google-uid-12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@gmail.com"))
                .thenReturn(Optional.empty());
        when(userCodeGenerator.generate()).thenReturn("USR-08-2026-00000006");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(20L);
            return u;
        });
        when(authProviderRepository.save(any(UserAuthProvider.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(eq("newuser@gmail.com"), anyMap()))
                .thenReturn("mock.jwt.token");

        SocialLoginRequest request = new SocialLoginRequest("GOOGLE", idToken);
        AuthResponse response = socialAuthService.authenticate(request);

        assertThat(response.token()).isEqualTo("mock.jwt.token");
        assertThat(response.fullName()).isEqualTo("New Google User");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("newuser@gmail.com") &&
                user.getPassword() == null &&
                user.getRole() == Role.ROLE_USER &&
                user.getIsActive()
        ));
        verify(authProviderRepository).save(argThat(provider ->
                provider.getProvider().equals("GOOGLE") &&
                provider.getProviderId().equals("google-uid-12345")
        ));
    }

    @Test
    void authenticate_Google_ExistingLinkedUser_ReturnsExisting() {
        String idToken = createSignedGoogleJwt("existing@gmail.com", "Existing User");
        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        User existingUser = User.builder()
                .id(5L).userCode("USR-01-2026-00000005")
                .email("existing@gmail.com").fullName("Existing User")
                .role(Role.ROLE_USER).isActive(true)
                .build();
        UserAuthProvider existingProvider = UserAuthProvider.builder()
                .id(1L).user(existingUser).provider("GOOGLE").providerId("google-uid-12345")
                .build();

        when(authProviderRepository.findByProviderAndProviderId("GOOGLE", "google-uid-12345"))
                .thenReturn(Optional.of(existingProvider));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(eq("existing@gmail.com"), anyMap()))
                .thenReturn("mock.jwt.existing");

        SocialLoginRequest request = new SocialLoginRequest("GOOGLE", idToken);
        AuthResponse response = socialAuthService.authenticate(request);

        assertThat(response.token()).isEqualTo("mock.jwt.existing");
        assertThat(response.userCode()).isEqualTo("USR-01-2026-00000005");

        // Should NOT create a new user
        verify(userRepository, never()).save(argThat(u -> u.getId() == null));
    }

    @Test
    void authenticate_Google_BlockedUser_ThrowsSecurityException() {
        String idToken = createSignedGoogleJwt("blocked@gmail.com", "Blocked User");
        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        User blockedUser = User.builder()
                .id(9L).email("blocked@gmail.com").fullName("Blocked User")
                .role(Role.ROLE_USER).isActive(false)
                .build();
        UserAuthProvider provider = UserAuthProvider.builder()
                .id(2L).user(blockedUser).provider("GOOGLE").providerId("google-uid-12345")
                .build();

        when(authProviderRepository.findByProviderAndProviderId("GOOGLE", "google-uid-12345"))
                .thenReturn(Optional.of(provider));

        SocialLoginRequest request = new SocialLoginRequest("GOOGLE", idToken);

        assertThatThrownBy(() -> socialAuthService.authenticate(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("blocked");
    }

    @Test
    void authenticate_Google_InvalidSignature_ThrowsSecurityException() {
        // Sign with a DIFFERENT key than what JWKS returns
        KeyPairGenerator kpg;
        KeyPair wrongKeyPair;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            wrongKeyPair = kpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String forgedToken = Jwts.builder()
                .header().keyId("test-kid-1").and()
                .subject("attacker")
                .claim("email", "attacker@evil.com")
                .claim("aud", "test-google-client-id")
                .issuer("https://accounts.google.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(wrongKeyPair.getPrivate())
                .compact();

        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        SocialLoginRequest request = new SocialLoginRequest("GOOGLE", forgedToken);

        assertThatThrownBy(() -> socialAuthService.authenticate(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("signature invalid");
    }

    @Test
    void authenticate_Google_WrongAudience_ThrowsSecurityException() {
        // Token with wrong audience
        String token = Jwts.builder()
                .header().keyId("test-kid-1").and()
                .subject("google-uid-12345")
                .claim("email", "user@gmail.com")
                .claim("name", "User")
                .claim("aud", "wrong-client-id")
                .issuer("https://accounts.google.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(rsaKeyPair.getPrivate())
                .compact();

        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        SocialLoginRequest request = new SocialLoginRequest("GOOGLE", token);

        assertThatThrownBy(() -> socialAuthService.authenticate(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("audience");
    }

    @Test
    void authenticate_Google_WrongIssuer_ThrowsSecurityException() {
        String token = Jwts.builder()
                .header().keyId("test-kid-1").and()
                .subject("google-uid-12345")
                .claim("email", "user@gmail.com")
                .claim("aud", "test-google-client-id")
                .issuer("https://evil-issuer.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(rsaKeyPair.getPrivate())
                .compact();

        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        SocialLoginRequest request = new SocialLoginRequest("GOOGLE", token);

        assertThatThrownBy(() -> socialAuthService.authenticate(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("issuer");
    }

    // ─── Facebook OAuth Tests ───

    @Test
    void authenticate_Facebook_NewUser_CreatesAccount() {
        mockFacebookResponses("fb-uid-999", "fbuser@gmail.com", "FB User");

        when(authProviderRepository.findByProviderAndProviderId("FACEBOOK", "fb-uid-999"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("fbuser@gmail.com"))
                .thenReturn(Optional.empty());
        when(userCodeGenerator.generate()).thenReturn("USR-08-2026-00000011");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(21L);
            return u;
        });
        when(authProviderRepository.save(any(UserAuthProvider.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(eq("fbuser@gmail.com"), anyMap()))
                .thenReturn("mock.fb.jwt");

        SocialLoginRequest request = new SocialLoginRequest("FACEBOOK", "fake-fb-access-token");
        AuthResponse response = socialAuthService.authenticate(request);

        assertThat(response.token()).isEqualTo("mock.fb.jwt");
        assertThat(response.fullName()).isEqualTo("FB User");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("fbuser@gmail.com") &&
                user.getPassword() == null
        ));
    }

    @Test
    void authenticate_Facebook_InvalidToken_ThrowsSecurityException() {
        Map<String, Object> debugData = new HashMap<>();
        debugData.put("is_valid", false);
        debugData.put("app_id", "test-fb-app-id");
        Map<String, Object> debugResponse = Map.of("data", debugData);

        when(restTemplate.getForObject(contains("debug_token"), eq(Map.class)))
                .thenReturn(debugResponse);

        SocialLoginRequest request = new SocialLoginRequest("FACEBOOK", "invalid-token");

        assertThatThrownBy(() -> socialAuthService.authenticate(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not valid");
    }

    @Test
    void authenticate_Facebook_WrongAppId_ThrowsSecurityException() {
        Map<String, Object> debugData = new HashMap<>();
        debugData.put("is_valid", true);
        debugData.put("app_id", "different-app-id");
        Map<String, Object> debugResponse = Map.of("data", debugData);

        when(restTemplate.getForObject(contains("debug_token"), eq(Map.class)))
                .thenReturn(debugResponse);

        SocialLoginRequest request = new SocialLoginRequest("FACEBOOK", "some-token");

        assertThatThrownBy(() -> socialAuthService.authenticate(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not issued for our app");
    }

    // ─── General Tests ───

    @Test
    void authenticate_UnsupportedProvider_ThrowsSecurityException() {
        SocialLoginRequest request = new SocialLoginRequest("TWITTER", "some-token");

        assertThatThrownBy(() -> socialAuthService.authenticate(request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Unsupported provider");
    }

    @Test
    void authenticate_Google_ExistingEmailUser_LinksWithoutCreatingNew() {
        String idToken = createSignedGoogleJwt("already@registered.com", "Already Registered");
        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        User existingUser = User.builder()
                .id(7L).userCode("USR-05-2026-00000007")
                .email("already@registered.com").fullName("Already Registered")
                .password("$2a$12$somehash").role(Role.ROLE_USER).isActive(true)
                .build();

        // Not linked via provider yet
        when(authProviderRepository.findByProviderAndProviderId("GOOGLE", "google-uid-12345"))
                .thenReturn(Optional.empty());
        // But email exists in users table
        when(userRepository.findByEmail("already@registered.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authProviderRepository.save(any(UserAuthProvider.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(eq("already@registered.com"), anyMap()))
                .thenReturn("mock.jwt.linked");

        SocialLoginRequest request = new SocialLoginRequest("GOOGLE", idToken);
        AuthResponse response = socialAuthService.authenticate(request);

        assertThat(response.userCode()).isEqualTo("USR-05-2026-00000007");
        // Should link the provider to existing user, not create new
        verify(authProviderRepository).save(argThat(p ->
                p.getUser().getId().equals(7L) &&
                p.getProvider().equals("GOOGLE")
        ));
    }

    // ─── linkProvider Tests ───

    @Test
    void linkProvider_Google_Success() {
        String idToken = createSignedGoogleJwt("linkme@gmail.com", "Link Me");
        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        User user = User.builder().id(3L).email("linkme@gmail.com").build();

        when(authProviderRepository.findByProviderAndProviderId("GOOGLE", "google-uid-12345"))
                .thenReturn(Optional.empty());
        when(authProviderRepository.save(any(UserAuthProvider.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        socialAuthService.linkProvider(user, "GOOGLE", idToken);

        verify(authProviderRepository).save(argThat(p ->
                p.getUser().getId().equals(3L) &&
                p.getProvider().equals("GOOGLE") &&
                p.getProviderId().equals("google-uid-12345")
        ));
    }

    @Test
    void linkProvider_AlreadyLinkedToDifferentUser_ThrowsSecurityException() {
        String idToken = createSignedGoogleJwt("taken@gmail.com", "Taken");
        when(restTemplate.getForObject(
                contains("googleapis.com/oauth2/v3/certs"), eq(Map.class)
        )).thenReturn(buildJwksResponse());

        User currentUser = User.builder().id(3L).email("current@gmail.com").build();
        User otherUser = User.builder().id(99L).email("taken@gmail.com").build();
        UserAuthProvider existingLink = UserAuthProvider.builder()
                .id(50L).user(otherUser).provider("GOOGLE").providerId("google-uid-12345")
                .build();

        when(authProviderRepository.findByProviderAndProviderId("GOOGLE", "google-uid-12345"))
                .thenReturn(Optional.of(existingLink));

        assertThatThrownBy(() -> socialAuthService.linkProvider(currentUser, "GOOGLE", idToken))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("already linked to a different user");
    }

    @Test
    void linkProvider_UnsupportedProvider_ThrowsSecurityException() {
        User user = User.builder().id(3L).build();

        assertThatThrownBy(() -> socialAuthService.linkProvider(user, "TWITTER", "token"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Unsupported provider");
    }
}