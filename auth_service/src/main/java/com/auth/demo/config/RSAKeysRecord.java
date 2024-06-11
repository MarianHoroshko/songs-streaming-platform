package com.auth.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "jwt")
public record RSAKeysRecord(RSAPublicKey rsaPublicKey, RSAPrivateKey rsaPrivateKey) {
}
