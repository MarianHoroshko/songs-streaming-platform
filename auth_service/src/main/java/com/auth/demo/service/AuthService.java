package com.auth.demo.service;

import com.auth.demo.dto.SignInDto;
import com.auth.demo.dto.SignUpDto;
import com.auth.demo.entity.RefreshTokenEntity;
import com.auth.demo.entity.UserEntity;
import com.auth.demo.enums.TokenType;
import com.auth.demo.jwt.JwtTokenGenerator;
import com.auth.demo.mapper.UserEntityMapper;
import com.auth.demo.repository.RefreshTokenRepository;
import com.auth.demo.repository.UserRepository;
import com.auth.demo.response.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenGenerator jwtTokenGenerator;

    private final UserEntityMapper userEntityMapper;

    private static Authentication createAuthentication(UserEntity userEntity) {
        // Extract user details from UserEntity
        String username = userEntity.getUsername();
        String pass = userEntity.getPassword();

        return new UsernamePasswordAuthenticationToken(username, pass);
    }

    public AuthResponse singInUser(SignInDto signInDto, HttpServletResponse response) {
        try {
            log.info("[AuthService:singInUser] User Authentication Started with ::: {}", signInDto.getUsername());

            UserEntity user = userRepository.findByUsername(signInDto.getUsername())
                    .orElseThrow(() -> {
                        log.error("[AuthService:singInUser] User: {} not found.", signInDto.getUsername());
                        return new UsernameNotFoundException("User: " + signInDto.getUsername() + " do not exist.");
                    });
            if (user == null) {
                log.error("[AuthService:singInUser] User: {} not found.", signInDto.getUsername());
                throw new UsernameNotFoundException("User: " + signInDto.getUsername() + " do not exist.");
            }

            Authentication authentication = createAuthentication(user);
            log.info("[AuthService:singInUser] User: {} successfully authenticated.", user.getUsername());

            return getJWTTokensAfterAuth(authentication, response);
        } catch (Exception e) {
            log.error("[AuthService:singInUser] Exception in authentication process the user due to: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private AuthResponse getJWTTokensAfterAuth(Authentication authentication, HttpServletResponse response) {
        try {
            UserEntity userInfoEntity = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> {
                        log.error("[AuthService:getJWTTokensAfterAuth] User :{} not found", authentication.getName());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "USER NOT FOUND ");
                    });


            String accessToken = jwtTokenGenerator.generateAccessToken(authentication);
            log.info("[AuthService:getJWTTokensAfterAuth] Access token for user: {}, has been generated", userInfoEntity.getUsername());

            String refreshToken = jwtTokenGenerator.generateRefreshToken(authentication);
            log.info("[AuthService:getJWTTokensAfterAuth] Refresh token for user: {}, has been generated", userInfoEntity.getUsername());

            saveUserRefreshToken(userInfoEntity, refreshToken);
            log.info("[AuthService:getJWTTokensAfterAuth] Refresh token for user: {}, has been saved", userInfoEntity.getUsername());

            createRefreshTokenCookie(response, refreshToken);
            log.info("[AuthService:getJWTTokensAfterAuth] Refresh token for user: {}, has been added to cookies", userInfoEntity.getUsername());

            return AuthResponse
                    .builder()
                    .accessToken(accessToken)
                    .accessTokenExpiry(15 * 60)
                    .username(userInfoEntity.getUsername())
                    .tokenType(TokenType.Bearer).build();
        } catch (Exception e) {
            log.error("[AuthService:getJWTTokensAfterAuth] Exception while authenticating the user due to :" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please Try Again.");
        }
    }

    public AuthResponse getAccessTokenUsingRefreshToken(String refreshToken) {
        // TODO: filter Token

        // check is refreshToken saved in db and not revoked
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh token revoked"));

        if (refreshTokenEntity.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh token revoked");
        }

        UserEntity user = userRepository.findByUsername(refreshTokenEntity.getUsername())
                .orElseThrow(() -> {
                    log.error("[AuthService:getAccessTokenUsingRefreshToken] User :{} not found", refreshTokenEntity.getUsername());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + refreshTokenEntity.getUsername() + " not found.");
                });

        // create Authentication object
        Authentication authentication = createAuthentication(user);

        String accessToken = jwtTokenGenerator.generateAccessToken(authentication);
        log.info("[AuthService:getAccessTokenUsingRefreshToken] Access token for user: {}, has been generated", user.getUsername());

        return AuthResponse.builder().accessToken(accessToken).accessTokenExpiry(15 * 60).username(user.getUsername()).tokenType(TokenType.Bearer).build();
    }

    public AuthResponse registerUser(SignUpDto signUpDto, HttpServletResponse response) {
        try {
            log.info("[AuthService:registerUser] User Registration Started with ::: {}", signUpDto.getUsername());

            Optional<UserEntity> user = userRepository.findByUsername(signUpDto.getUsername());
            if (user.isPresent()) {
                throw new BadRequestException("User Already Exist");
            }

            UserEntity userDetailsEntity = userEntityMapper.convertToUserEntity(signUpDto);
            userDetailsEntity.setCreatedAt(Instant.now());
            userDetailsEntity.setUpdatedAt(Instant.now());

            Authentication authentication = createAuthentication(userDetailsEntity);

            // Generate a JWT token
            String accessToken = jwtTokenGenerator.generateAccessToken(authentication);
            String refreshToken = jwtTokenGenerator.generateRefreshToken(authentication);

            userRepository.save(userDetailsEntity);
            saveUserRefreshToken(userDetailsEntity, refreshToken);

            createRefreshTokenCookie(response, refreshToken);

            log.info("[AuthService:registerUser] User:{} Successfully registered", signUpDto.getUsername());
            return AuthResponse.builder().accessToken(accessToken).accessTokenExpiry(5 * 60).username(signUpDto.getUsername()).tokenType(TokenType.Bearer).build();
        } catch (Exception e) {
            log.error("[AuthService:registerUser] Exception while registering the user due to: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void saveUserRefreshToken(UserEntity user, String refreshToken) {
        log.info("[AuthService:saveUserRefreshToken] Saving refresh token.");
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity
                .builder()
                .username(user.getUsername())
                .refreshToken(refreshToken)
                .revoked(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
    }

    private void createRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge(15 * 24 * 60 * 60); // in seconds

        response.addCookie(refreshTokenCookie);
    }
}
