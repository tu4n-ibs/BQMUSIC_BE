package com.example.demo.service;

import com.example.demo.common.AppException;
import com.example.demo.common.TokenExpiredException;
import com.example.demo.entity.*;
import com.example.demo.model.LoginResponse;
import com.example.demo.model.enum_object.Provider;
import com.example.demo.model.RefreshTokenRequest;
import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    @Value("${token.secret.key}")
    private String secret;

    private final RefreshTokenRepository refreshTokenRepository;


    public String createJwt(UserEntity user) throws JOSEException {
        JWSHeader jweHeader = new JWSHeader(JWSAlgorithm.HS256);

        List<String> roles = user.getRoles()
                .stream()
                .map(RoleEntity::getName)
                .toList();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("DANG QUAN BAO")
                .claim("userId", user.getId())
                .claim("roles", roles)
                .expirationTime(new Date(System.currentTimeMillis() + 6000000))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSObject jwsObject = new JWSObject(jweHeader, jwtClaimsSet.toPayload());

        jwsObject.sign(new MACSigner(secret.getBytes()));

        return jwsObject.serialize();
    }

    public LoginResponse createRefreshToken(UserEntity user) throws JOSEException, ParseException {
        String refreshToken = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 24L * 60 * 60 * 1000);
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(refreshToken, user.getId(), expiresAt);
        refreshTokenRepository.save(refreshTokenEntity);
        String token = createJwt(user);
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Đã có sẵn theo mẫu
        UserEntity userEntity = userRepository.findByEmail(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "Cannot find user"));

        Set<RoleEntity> roles = userEntity.getRoles();
        Set<String> roleSet = roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
        return new LoginResponse(token, refreshToken, roleSet, userEntity.getId(), userEntity.getEmail(), userEntity.getName(), userEntity.getImageUrl());
    }

    public LoginResponse login(String email, String password) throws JOSEException, ParseException {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "AUTH_NF_001", "User not found"));
        if (userEntity.getIsActive().equals(false)) {
            throw new  AppException(HttpStatus.CONFLICT,"USER_BLOCK","User blocked");
        }
        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Invalid password");
        }
        return createRefreshToken(userEntity);
    }

    public SignedJWT filterToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(secret.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new TokenExpiredException("Token expired or invalid");
        }
        return signedJWT;
    }

    public String logout(String token, RefreshTokenRequest request) throws ParseException, JOSEException {
        var signedJWT = filterToken(token);
        var claims = signedJWT.getJWTClaimsSet();
        InvalidatedTokenEntity invalidatedTokenEntity = new InvalidatedTokenEntity(token, claims.getExpirationTime());
        refreshTokenRepository.deleteById(request.refreshToken());
        invalidatedTokenRepository.save(invalidatedTokenEntity);
        return "Logout successful";
    }

    public Boolean isTokenInBlackList(String token) {
        return invalidatedTokenRepository.existsById(token);
    }

    public String refreshToken(String refreshToken) throws JOSEException {
        RefreshTokenEntity refreshTokenEntity = isRefreshTokenValid(refreshToken);

        UserEntity user = userRepository.findById(refreshTokenEntity.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_002", "User not found for this token"));

        return createJwt(user);
    }

    public RefreshTokenEntity isRefreshTokenValid(String token) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(token)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "REFRESH_TOKEN_NF", "Refresh token not found"));

        if (!userRepository.existsById(refreshTokenEntity.getUserId())) {
            throw new AppException(HttpStatus.NOT_FOUND, "USER_NF_003", "User of this token not found");
        }
        if (refreshTokenEntity.getExpiresTime().before(new Date())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Refresh token expired");
        }
        return refreshTokenEntity;
    }

    @Transactional
    public String processOAuthPostLogin(OAuth2User oAuth2User) {
        try {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");

            UserEntity user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        UserEntity newUser = new UserEntity();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setImageUrl(picture);
                        newUser.setIsActive(true);
                        newUser.setProvider(Provider.GOOGLE);
                        return userRepository.save(newUser);
                    });

            LoginResponse loginResponse = createRefreshToken(user);

            String token = loginResponse.getToken();
            String refreshToken = loginResponse.getRefreshToken();
            String roles = user.getRoles().stream()
                    .map(RoleEntity::getName)
                    .collect(Collectors.joining(","));
            String encodedName = name != null ? URLEncoder.encode(name, StandardCharsets.UTF_8) : "";
            return String.format(
                    "http://localhost:3000/oauth2/redirect?token=%s&refreshToken=%s&roles=%s&email=%s&name=%s&imageUrl=%s",
                    token, refreshToken, roles, email, encodedName, picture
            );

        } catch (Exception e) {
            return "http://localhost:3000/login?error=true";
        }
    }
}