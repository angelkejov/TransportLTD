package com.example.transportGrup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyUserDto {
    private String email;
    private String verificationCode;

    public VerifyUserDto(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
    }
}