package com.viarr.viarr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileDto {
    private String username;
    private String fullName;
    private String email;
    private List<String> skills;
}
