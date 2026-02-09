package com.example.demo.controller;

import com.example.demo.entity.LogCrud;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.LoginResponse;
import com.example.demo.model.RefreshTokenRequest;
import com.example.demo.service.AuthService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    @PostMapping
    public LoginResponse login(@RequestBody LoginRequest loginRequest) throws JOSEException, ParseException {
        return authService.login(loginRequest.email(),loginRequest.password());
    }
    @PostMapping("/logout")
    public String logout(@AuthenticationPrincipal Jwt jwt, @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        return authService.logout(jwt.getTokenValue(),request);
    }
    @PostMapping("/refresh-token")
    public String refreshToken(@RequestBody RefreshTokenRequest request) throws JOSEException {
        return authService.refreshToken(request.refreshToken());
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<LogCrud> history(@ParameterObject Pageable pageable){
        return authService.history(pageable);
    }
}