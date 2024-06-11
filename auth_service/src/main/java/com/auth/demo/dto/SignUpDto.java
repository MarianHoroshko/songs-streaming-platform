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
public class SignUpDto {

    @JsonProperty("username")
//    @NotEmpty(message = "Username can not be empty.")
    private String username;

    @JsonProperty("email")
//    @NotEmpty(message = "Email can not be empty.")
//    @Email(message = "Invalid email message.")
    private String email;

    @JsonProperty("password")
//    @NotEmpty(message = "Password can not be empty.")
    private String password;

    @JsonProperty("user_roles")
//    @NotEmpty(message = "User roles can not be empty.")
    private String userRoles;
}
