package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.dto.QuestionEvaluation;
import com.viarr.viarr_backend.model.InterviewResponse;
import com.viarr.viarr_backend.model.Question;
import com.viarr.viarr_backend.model.GeminiScoreResponse;
import com.viarr.viarr_backend.repository.InterviewResponseRepository;
import com.viarr.viarr_backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final InterviewResponseRepository responseRepository;
    private final GeminiService geminiService;

    public List<QuestionEvaluation> evaluateUserResponses(Long userId) {

        List<InterviewResponse> responses = responseRepository.findByUserId(userId);

        if (responses.isEmpty()) {
            return Collections.singletonList(new QuestionEvaluation(
                    "No interview done",
                    "No responses available to evaluate",
                    0,
                    "Please complete the interview first."
            ));
        }

        List<QuestionEvaluation> evaluations = new ArrayList<>();

        for (InterviewResponse response : responses) {
            Question question = response.getQuestion();
            String questionText = question.getQuestionText();
            String answer = response.getAnswer();

            GeminiScoreResponse scoreResponse = geminiService.evaluateAnswer(questionText, answer);

            evaluations.add(new QuestionEvaluation(
                    questionText,
                    answer,
                    scoreResponse.getScore(),
                    scoreResponse.getFeedback()
            ));
        }

        return evaluations;
    }
}
