package com.sciarticles.manager.model;

import lombok.Data;

import java.util.UUID;

@Data
public class UserRequest {
    private String email;
    private String password;
}