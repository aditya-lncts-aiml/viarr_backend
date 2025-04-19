package com.viarr.viarr_backend.service;


import com.viarr.viarr_backend.model.GeminiScoreResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String apiKey;


    public String getQuestionsFromLLM(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Prepare request body
            Map<String, Object> requestBody = buildRequestBody(prompt);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the POST request
            String fullUrl = geminiApiUrl + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(fullUrl, requestEntity, Map.class);

            // Extract response text
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            return "Exception while calling Gemini: " + e.getMessage();
        }
        return "Error: Could not fetch response from Gemini.";
    }

// Interview scoring
public GeminiScoreResponse evaluateAnswer(String question, String answer) {
    String prompt = generatePrompt(question, answer);
    Map<String, Object> requestBody = buildRequestBody(prompt);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    String fullUrl = geminiApiUrl + "?key=" + apiKey; // ✅ key passed in query param

    ResponseEntity<Map> response = new RestTemplate().postForEntity(fullUrl, entity, Map.class);

    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        // 🌟 parse response properly below
        String responseText = extractTextFromGeminiResponse(response.getBody());
        return GeminiScoreResponse.parse(responseText);
    }

    return new GeminiScoreResponse(0, "Error: Could not get a valid response.");
}


    private String generatePrompt(String question, String answer) {
        return String.format("""
            Evaluate the following answer to the interview question. Score from 1 to 10 and explain in 1-2 sentences.

            Question: %s
            Answer: %s

            Response Format:
            Score: <score>
            Feedback: <brief feedback no longer than 255 varchar>
        """, question, answer);
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );
    }

    private String extractTextFromGeminiResponse(Map body) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            return "Error parsing Gemini response: " + e.getMessage();
        }
        return "No valid content in Gemini response.";
    }

}
