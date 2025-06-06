package com.example.transportGrup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {
    private String email;
    private String password;
    private String username;

    public RegisterUserDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

}