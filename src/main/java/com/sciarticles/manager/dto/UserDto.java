package com.sciarticles.manager.dto;

import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Getter
    private String id;
    private String email;
    private String password;
    private String name;
    private String role;
    private Timestamp created_at;

    public UserDto(String email, String hashedPassword , String role) {
        this.email = email;
        this.password = hashedPassword;
        this.role = role;
    }

}