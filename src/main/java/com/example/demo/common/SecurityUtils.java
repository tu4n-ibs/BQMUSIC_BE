package com.example.demo.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {

    public static String getCurrentUserEmail() {
        Jwt jwt = getJwt();
        return jwt.getSubject();
    }

    public static String getCurrentUserId() {
        Jwt jwt = getJwt();
        return jwt.getClaim("userId");
    }
    public static String getCurrentRoles() {
        Jwt jwt = getJwt();
        return jwt.getClaim("roles");
    }
    private static Jwt getJwt() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("No authenticated user found");
        }

        return (Jwt) authentication.getPrincipal();
    }
}