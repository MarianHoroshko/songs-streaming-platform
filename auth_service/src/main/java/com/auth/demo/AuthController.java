package com.auth.demo;

import com.auth.demo.dto.SignInDto;
import com.auth.demo.dto.SignUpDto;
import com.auth.demo.response.AuthResponse;
import com.auth.demo.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody SignInDto signInDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.singInUser(signInDto, response));
    }

    // NOTE: if validation don't work add BindingResult
    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody SignUpDto signUpDto, HttpServletResponse response) {
        log.info("[AuthController:registerUser] Start of registration process for user: {}", signUpDto.getUsername());

        return ResponseEntity.ok(authService.registerUser(signUpDto, response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> getAccessTokenFromRefreshToken(@CookieValue("refresh_token") String refreshToken) {
        return ResponseEntity.ok(authService.getAccessTokenUsingRefreshToken(refreshToken));
    }
}
