package com.viarr.viarr_backend.controller;

import com.viarr.viarr_backend.dto.JobDescriptionRequest;
import com.viarr.viarr_backend.service.ATSService;
import com.viarr.viarr_backend.service.CustomUserDetailsService;
import com.viarr.viarr_backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ats-score")
@Slf4j
@RequiredArgsConstructor
public class ATSController {

    private final ResumeService resumeService;
    private final CustomUserDetailsService userDetailsService;



    @Autowired
    private ATSService atsService;

    @PostMapping
    public ResponseEntity<?> calculateAtsScore(@RequestBody JobDescriptionRequest request, Principal principal) {
        if (principal == null) {
            log.warn("Principal is null! User not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String username = principal.getName();
        System.out.println("Requesting ATS score for user: " + username);

        // ✅ Try fetching resume file
        byte[] fileBytes;
        try {
            fileBytes = resumeService.getResumeFileBytes(username);
        } catch (RuntimeException e) {
            // 🎯 Catch exception thrown by ResumeService if no resume found
            log.warn("No resume found for user: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Resume not found for user: " + username));
        }

        // ✅ Extract text & calculate score
        String resumeText = resumeService.extractTextFromResume(fileBytes);
        double score = atsService.calculateScore(resumeText, request.getJobDescription());

        Map<String, Object> response = new HashMap<>();
        response.put("score", score);
        return ResponseEntity.ok(response);
    }


}
