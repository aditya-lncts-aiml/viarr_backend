package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.model.Question;
import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.repository.QuestionRepository;
import com.viarr.viarr_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionGenerator questionGenerator;
    private final LLMQuestionService llmQuestionService;
    private final UserRepository userRepository;

    public List<Question> generateAndSaveQuestions(Map<String, List<String>> extractedData, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        questionRepository.deleteAllByUserUsername(username);

        List<Question> aiQuestions = llmQuestionService.generateQuestionsFromKeywords(extractedData, user);

        if (aiQuestions.size() < 25) {
            int remaining = 25 - aiQuestions.size();
            List<Question> genericQuestions = questionGenerator.generateGenericQuestions(remaining, user);
            aiQuestions.addAll(genericQuestions);
        }

        aiQuestions.forEach(q -> q.setUser(user));
        return questionRepository.saveAll(aiQuestions.subList(0, Math.min(25, aiQuestions.size())));
    }

}