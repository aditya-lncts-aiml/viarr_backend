package com.viarr.viarr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEvaluation {
    private String question;
    private String answer;
    private int score;
    private String feedback;
}
