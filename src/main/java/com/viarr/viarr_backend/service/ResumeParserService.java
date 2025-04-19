package com.viarr.viarr_backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ResumeParserService {

    private final NLPService nlpService;

    public ResumeParserService(NLPService nlpService) {
        this.nlpService = nlpService;
    }

    // ✅ Extract text from resume file
    public String extractTextFromResume(byte[] fileBytes) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);

            // Clean and normalize encoding
            String cleanText = new String(rawText.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            cleanText = cleanText.replaceAll("[^\\x00-\\x7F]", " "); // remove/control junk chars
            System.out.println("Resume text is printed from Resume Parser Service (extractTextFromResume method)");
            System.out.println(cleanText);
            return cleanText.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ Extract keywords from resume text
    public Map<String, List<String>> extractKeywords(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        Map<String, List<String>> extracted = new HashMap<>();

        // Extract skills using NLP entity recognition
        List<String> skills = nlpService.extractSkills(text);
        List<String> education = nlpService.extractEducation(text);
        List<String> certifications = nlpService.extractCertifications(text);
        List<String> accomplishments = nlpService.extractAccomplishments(text);

        extracted.put("skills", skills);
        extracted.put("education", education);
        extracted.put("certifications", certifications);
        extracted.put("accomplishments", accomplishments);

        return extracted;
    }
}
