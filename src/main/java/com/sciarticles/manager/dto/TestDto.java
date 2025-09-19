package com.sciarticles.manager.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDto {
    private String username;
    private String email;
    private String password_hash;
}