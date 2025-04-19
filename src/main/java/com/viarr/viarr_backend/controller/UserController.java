package com.viarr.viarr_backend.controller;

import com.viarr.viarr_backend.dto.UserProfileDto;
import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.service.CustomUserDetailsService;
import com.viarr.viarr_backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user-service")
@RequiredArgsConstructor
public class UserController {

    private final CustomUserDetailsService userService;
    private final ResumeService resumeService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> skills = resumeService.getSkillsByUserId(userId); // You must define this method

        UserProfileDto profileDto = new UserProfileDto(
                user.getUsername(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                skills
        );

        return ResponseEntity.ok(profileDto);
    }
}
