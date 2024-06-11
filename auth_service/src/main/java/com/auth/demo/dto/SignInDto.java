package com.auth.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInDto {

    @JsonProperty("username")
//    @NotEmpty(message = "Username can not be empty.")
    private String username;

    @JsonProperty("password")
//    @NotEmpty(message = "Password can not be empty.")
    private String password;
}
