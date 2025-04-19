package com.viarr.viarr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String status;
    private String token;
}
