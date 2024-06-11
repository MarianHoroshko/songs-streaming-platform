package com.auth.demo.service;

import com.auth.demo.config.RSAKeysRecord;
import com.auth.demo.entity.RefreshTokenEntity;
import com.auth.demo.enums.TokenType;
import com.auth.demo.jwt.JwtTokenUtils;
import com.auth.demo.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutHandlerService implements LogoutHandler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RSAKeysRecord rsaKeysRecord;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("[LogoutHandlerService:logout] Logout process started.");

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!authHeader.startsWith(TokenType.Bearer.name())) {
            log.error("[LogoutHandlerService:logout] Incorrect access token type.");
            return;
        }

        JwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaKeysRecord.rsaPublicKey()).build();
        final String authToken = authHeader.substring(7);
        final Jwt jwt = jwtDecoder.decode(authToken);

        final String username = jwtTokenUtils.getUsername(jwt);
        log.info("[LogoutHandlerService:logout] Revoking refresh token for user: {}", username);

        final Cookie refreshTokenCookie = WebUtils.getCookie(request, "refresh_token");
        if (refreshTokenCookie == null) {
            throw new BadCredentialsException("Refresh token not found.");
        }

        final String refreshToken = refreshTokenCookie.getValue();

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("[LogoutHandlerService:logout] Refresh token not found."));

        log.info("[LogoutHandlerService:logout] Got refresh token entity for: {}", refreshTokenEntity.getUsername());

        refreshTokenEntity.setRevoked(true);
        refreshTokenEntity.setUpdatedAt(Instant.now());
        refreshTokenRepository.update(refreshTokenEntity);
    }
}
