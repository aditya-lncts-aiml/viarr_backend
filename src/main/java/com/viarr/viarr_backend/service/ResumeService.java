package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.model.Resume;
import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.repository.QuestionRepository;
import com.viarr.viarr_backend.repository.ResumeRepository;
import com.viarr.viarr_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ResumeParserService resumeParserService;

    // Fetch resume from DB and return as File (used in ATSController)
    public byte[] getResumeFileBytes(String username) {
        Resume resume = resumeRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Resume not found for user: " + username));
        return resume.getFileData();
    }

    // Extract raw text from InputStream (used elsewhere if needed)
    public String extractTextFromResume(byte[] fileData) {
        try (InputStream is = new ByteArrayInputStream(fileData);
             PDDocument document = PDDocument.load(is)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);

        } catch (IOException e) {
            log.error("Error reading PDF from byte array: {}", e.getMessage());
            throw new RuntimeException("Failed to extract text from resume");
        }
    }


    @Transactional
    public void saveResume(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Resume existingResume = resumeRepository.findByUser(user);
        if (existingResume != null) {
            questionRepository.deleteAllByUserUsername(username);
            resumeRepository.delete(existingResume); // This must happen in a transaction
        }

        Resume resume = new Resume();
        resume.setFileName(file.getOriginalFilename());
        resume.setFileData(file.getBytes());
        resume.setUser(user);

        resumeRepository.save(resume);
    }

    public List<String> getSkillsByUserId(Long userId) {
        Resume resume = resumeRepository.findByUserId(userId);
        if (resume != null) {
            String text = resumeParserService.extractTextFromResume(resume.getFileData());
            return resumeParserService.extractKeywords(text).getOrDefault("skills", new ArrayList<>());
        }
        return new ArrayList<>();
    }

    public Resume getResumeByUserId(Long userId) {
        return resumeRepository.findByUserId(userId); // Assuming `findByUserId` method is already implemented
    }

}
