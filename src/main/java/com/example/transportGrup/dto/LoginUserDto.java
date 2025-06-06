package com.example.transportGrup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDto {
    private String email;
    private String password;

    public LoginUserDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}