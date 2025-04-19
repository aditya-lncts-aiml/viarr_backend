package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.model.*;
import com.viarr.viarr_backend.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InterviewService {

    private final InterviewResponseRepository responseRepo;
    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final GeminiService geminiService;

    private Map<Long, Long> currentQuestions = new HashMap<>();
    private Map<Long, Integer> userQuestionCount = new HashMap<>();

    public void saveUserResponse(Long userId, Long questionId, String answer) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Question question = questionRepo.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));

        // Evaluate using Gemini
        GeminiScoreResponse result = geminiService.evaluateAnswer(question.getQuestionText(), answer);

        // Store everything
        InterviewResponse response = new InterviewResponse();
        response.setUser(user);
        response.setQuestion(question);
        response.setAnswer(answer);
        response.setScore(result.getScore());
        response.setFeedback(result.getFeedback());

        responseRepo.save(response);
    }

    public ResponseEntity<Map<String, Object>> nextQuestion(Long userId) {
        List<Question> userQuestions = questionRepo.findByUser_Id(userId);

        if (userQuestions.isEmpty()) {
            return ResponseEntity.ok()
                    .body(Collections.singletonMap("message", "No questions found. Please upload your resume to generate questions on Dashboard."));
        }

        List<Question> unansweredQuestions = userQuestions.stream()
                .filter(q -> !currentQuestions.containsValue(q.getId()) && !isQuestionAnswered(userId, q.getId()))
                .collect(Collectors.toList());

        if (!unansweredQuestions.isEmpty()) {
            Collections.shuffle(unansweredQuestions);
            Question nextQuestion = unansweredQuestions.get(0);

            currentQuestions.put(userId, nextQuestion.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("questionText", nextQuestion.getQuestionText());
            response.put("questionId", nextQuestion.getId());

            return ResponseEntity.ok(response);
        }

        currentQuestions.remove(userId);
        return ResponseEntity.ok()
                .body(Collections.singletonMap("message", "All questions answered. Starting over."));
    }

    public void incrementUserQuestionCount(Long userId) {
        userQuestionCount.put(userId, userQuestionCount.getOrDefault(userId, 0) + 1);
    }

    public int getUserQuestionCount(Long userId) {
        return userQuestionCount.getOrDefault(userId, 0);
    }

    private boolean isQuestionAnswered(Long userId, Long questionId) {
        return responseRepo.existsByUserIdAndQuestionId(userId, questionId);
    }

    public void resetUserQuestionCount(Long userId) {
        userQuestionCount.put(userId, 0);
    }
}
