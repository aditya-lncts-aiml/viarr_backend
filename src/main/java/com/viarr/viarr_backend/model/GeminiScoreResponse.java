package com.viarr.viarr_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiScoreResponse {
    private int score;
    private String feedback;

    public static GeminiScoreResponse parse(String responseText) {
        int score = 0;
        String feedback = "";

        try {
            String[] lines = responseText.split("\n");

            for (String line : lines) {
                if (line.toLowerCase().contains("score:")) {
                    score = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                } else if (line.toLowerCase().contains("feedback:")) {
                    feedback = line.replaceFirst("(?i)feedback:", "").trim();
                }
            }

        } catch (Exception e) {
            feedback = "Error parsing response: " + e.getMessage();
        }

        return new GeminiScoreResponse(score, feedback);
    }
}
