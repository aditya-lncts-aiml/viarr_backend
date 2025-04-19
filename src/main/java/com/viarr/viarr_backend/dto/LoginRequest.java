package com.viarr.viarr_backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String email; // Optional
    private String password;
}
