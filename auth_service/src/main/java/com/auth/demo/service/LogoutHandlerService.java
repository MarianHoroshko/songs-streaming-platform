package com.auth.demo.service;

import com.auth.demo.config.RSAKeysRecord;
import com.auth.demo.entity.RefreshTokenEntity;
import com.auth.demo.enums.TokenType;
import com.auth.demo.jwt.JwtTokenUtils;
import com.auth.demo.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.List;

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
        final String refreshToken = authHeader.substring(7);
        final Jwt jwt = jwtDecoder.decode(refreshToken);

        final String username = jwtTokenUtils.getUsername(jwt);

        log.info("[LogoutHandlerService:logout] Revoking refresh tokens for user: {}", username);

        List<RefreshTokenEntity> refreshTokenEntityList = refreshTokenRepository
                .findAllRefreshTokenByUsername(username)
                .stream()
                .peek(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.update(token);
                })
                .toList();
    }
}
