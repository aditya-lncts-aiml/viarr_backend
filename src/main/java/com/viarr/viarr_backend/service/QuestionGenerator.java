package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.model.Question;
import com.viarr.viarr_backend.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionGenerator {

    public List<Question> generateGenericQuestions(int count, User user) {
        List<Question> questions = new ArrayList<>();
        String[] genericQuestions = {
                "What are your greatest strengths?",
                "What is your biggest weakness?",
                "Where do you see yourself in 5 years?",
                "Why are you interested in this position?",
                "Describe a challenge you've faced at work and how you overcame it.",
                "What are you looking for in your next role?",
                "How do you handle pressure or stressful situations?",
                "Describe your ideal work environment.",
                "How do you prioritize your work?",
                "What makes you unique?"
        };
        for (int i = 0; i < count && i < genericQuestions.length; i++) {
            questions.add(new Question(null, genericQuestions[i], "Generic", user));
        }
        return questions;
    }
}
