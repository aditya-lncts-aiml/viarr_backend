package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.model.Question;
import com.viarr.viarr_backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LLMQuestionService {

    private final GeminiService geminiService; // or your Gemini equivalent

    public List<Question> generateQuestionsFromKeywords(Map<String, List<String>> keywordMap, User user) {
        String prompt = buildPrompt(keywordMap);

        String response = geminiService.getQuestionsFromLLM(prompt); // This sends the prompt and returns raw string

        List<String> questionTexts = parseLLMResponse(response);
        return questionTexts.stream()
                .map(q -> new Question(null, q, "LLM",user))
                .collect(Collectors.toList());
    }

    private String buildPrompt(Map<String, List<String>> keywords) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate 25 diverse, thoughtful and easy or basic interview questions based on the following resume details and don't add indexing with questions:\n");

        keywords.forEach((category, values) -> {
            sb.append(category).append(": ").append(String.join(", ", values)).append("\n");
        });

        sb.append("Return only the questions, one per line.");
        return sb.toString();
    }

    private List<String> parseLLMResponse(String response) {
        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(q -> !q.isEmpty())
                .collect(Collectors.toList());
    }
}

