package com.viarr.viarr_backend.controller;

import com.viarr.viarr_backend.dto.ResumeAnalysisResult;
import com.viarr.viarr_backend.dto.ResumeUploadResponse;
import com.viarr.viarr_backend.model.Question;
import com.viarr.viarr_backend.model.Resume;
import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.repository.ResumeRepository;
import com.viarr.viarr_backend.repository.UserRepository;
import com.viarr.viarr_backend.service.QuestionService;
import com.viarr.viarr_backend.service.ResumeParserService;
import com.viarr.viarr_backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeParserService resumeParserService;
    private final QuestionService questionService;
    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, Principal principal) {
        try {
            long maxFileSize = 10 * 1024 * 1024;
            if (file.isEmpty()) return ResponseEntity.badRequest().body("Error: File is empty.");
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) return ResponseEntity.badRequest().body("Error: Only PDF files are supported.");
            if (!"application/pdf".equals(file.getContentType())) return ResponseEntity.badRequest().body("Error: Uploaded file is not a valid PDF.");
            if (file.getSize() > maxFileSize) return ResponseEntity.badRequest().body("Error: File size exceeds 10MB limit.");

            log.info("Resume upload attempt by user: {}", principal.getName());

            // ✅ Just call the service to handle saving (delete + save inside)
            resumeService.saveResume(principal.getName(), file);

            byte[] fileBytes = file.getBytes();
            String resumeText = resumeParserService.extractTextFromResume(fileBytes);
            if (resumeText == null || resumeText.isEmpty()) {
                return ResponseEntity.status(500).body("Error: Unable to extract text from resume.");
            }

            Map<String, List<String>> extractedInfo = resumeParserService.extractKeywords(resumeText);
            List<Question> questions = questionService.generateAndSaveQuestions(extractedInfo, principal.getName());

            return ResponseEntity.ok(new ResumeUploadResponse("Resume uploaded and parsed successfully", extractedInfo, questions));

        } catch (IOException e) {
            log.error("File processing error", e);
            return ResponseEntity.status(500).body("Error: Unable to read uploaded file.");
        } catch (Exception e) {
            log.error("Unexpected error during resume upload", e);
            return ResponseEntity.status(500).body("Error: Failed to process resume.");
        }
    }

    @GetMapping("/analysis/{userId}")
    public ResponseEntity<?> getResumeAnalysis(@PathVariable Long userId) {
        try {
            // Fetch resume by user ID
            Resume resume = resumeService.getResumeByUserId(userId);
            if (resume == null) {
                return new ResponseEntity<>("Resume not found for this user", HttpStatus.NOT_FOUND);
            }

            // Extract text from resume
            String resumeText = resumeParserService.extractTextFromResume(resume.getFileData());

            // Extract keywords (skills, education, certifications, accomplishments)
            Map<String, List<String>> extractedKeywords = resumeParserService.extractKeywords(resumeText);

            // Create a ResumeAnalysisResult object using the extracted data
            ResumeAnalysisResult result = new ResumeAnalysisResult(
                    extractedKeywords.get("skills"),
                    extractedKeywords.get("education"),
                    extractedKeywords.get("certifications"),
                    extractedKeywords.get("accomplishments")
            );

            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch resume analysis: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}