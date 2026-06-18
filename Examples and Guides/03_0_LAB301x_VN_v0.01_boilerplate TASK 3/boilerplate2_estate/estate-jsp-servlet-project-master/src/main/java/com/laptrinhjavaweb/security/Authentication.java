package com.laptrinhjavaweb.security;

import javax.servlet.http.HttpServletRequest;

public interface Authentication {
    String getRedirectUrl(HttpServletRequest request);

    static Authentication of(String username, String password) {
        return new AuthenticationImpl(username, password);
    }
}
