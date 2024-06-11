package com.auth.demo.mapper;

import com.auth.demo.dto.SignUpDto;
import com.auth.demo.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEntityMapper {
    private final PasswordEncoder passwordEncoder;

    public UserEntity convertToUserEntity(SignUpDto signUpDto) {
        UserEntity userEntity = new UserEntity();

        userEntity.setUsername(signUpDto.getUsername());
        userEntity.setEmail(signUpDto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        return userEntity;
    }
}
