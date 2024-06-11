package com.auth.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {
    private String refreshToken;

    private boolean revoked;

    private String username;

    private Instant createdAt;

    private Instant updatedAt;
}
