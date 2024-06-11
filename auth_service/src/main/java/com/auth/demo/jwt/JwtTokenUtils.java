package com.auth.demo.jwt;

import com.auth.demo.config.UserInfoConfig;
import com.auth.demo.entity.UserEntity;
import com.auth.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    private final UserRepository userRepository;

    public String getUsername(Jwt jwt) {
        return jwt.getSubject();
    }

    public boolean isTokenValid(Jwt jwt, UserDetails userDetails) {
        final String username = getUsername(jwt);
        final boolean isTokenExpired = Objects.requireNonNull(jwt.getExpiresAt()).isBefore(Instant.now());
        final boolean isUsernameSameInDb = username.equals(userDetails.getUsername());
        return !isTokenExpired && !isUsernameSameInDb;
    }

    public UserDetails getUserDetails(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("UserEmail: " + username + " does not exist"));

        UserDetails userDetails = new UserInfoConfig(user);

        return userDetails;
    }
}
