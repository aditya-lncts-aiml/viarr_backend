package com.viarr.viarr_backend.controller;

import com.viarr.viarr_backend.dto.QuestionEvaluation;
import com.viarr.viarr_backend.model.InterviewResponse;
import com.viarr.viarr_backend.model.Question;
import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.repository.InterviewResponseRepository;
import com.viarr.viarr_backend.repository.QuestionRepository;
import com.viarr.viarr_backend.repository.UserRepository;
import com.viarr.viarr_backend.service.EvaluationService;
import com.viarr.viarr_backend.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class InterviewController {

    private final InterviewService interviewService;
    private final QuestionRepository questionRepo;
    private final InterviewResponseRepository responseRepo;
    private final UserRepository userRepository;
    private final EvaluationService evaluationService;

    // Track current question start times per user
    private Map<Long, Long> questionTimers = new ConcurrentHashMap<>();

    // Track current question IDs per user
    private Map<Long, Long> currentQuestions = new ConcurrentHashMap<>();

    // Track how many questions user has answered in current session
    private Map<Long, Integer> userQuestionCount = new ConcurrentHashMap<>();

    /**
     * Resets the interview session for the current user.
     */
    @PostMapping("/startFresh")
    public ResponseEntity<Map<String, String>> startFreshInterview() {
        log.info("Starting fresh interview session");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Authenticated user: {}", username);

            // Fetch user
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));

            Long userId = user.getId();

            // Clear old interview state
            currentQuestions.remove(userId);
            questionTimers.remove(userId);
            userQuestionCount.put(userId, 0);
            responseRepo.deleteAllByUserId(userId);
            interviewService.resetUserQuestionCount(userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Interview session reset successfully.");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("Error resetting interview session: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error resetting interview session: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reset interview session", e);
        }
    }

    /**
     * Handles the answer submitted by the user and fetches the next question.
     */
    @PostMapping("/respond")
    public ResponseEntity<Map<String, Object>> respond(@RequestParam String answer) {
        log.info("Processing response for answer: {}", answer);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));

            Long userId = user.getId();

            // Increment the question count for this user
            interviewService.incrementUserQuestionCount(userId);

            // Check if this is the 10th response — interview ends after this
            if (interviewService.getUserQuestionCount(userId) >= 10) {
                Long questionId = currentQuestions.get(userId);
                if (questionId != null) {
                    interviewService.saveUserResponse(userId, questionId, answer);
                }

                // Clear state after last question
                currentQuestions.remove(userId);
                questionTimers.remove(userId);

                // Final message
                return ResponseEntity.ok().body(Collections.singletonMap("message", "You have completed 10 questions. The interview is over."));
            }

            Long questionId = currentQuestions.get(userId);
            if (questionId == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No active question for user."));
            }

            // Save the current response
            interviewService.saveUserResponse(userId, questionId, answer);

            // Remove current question so next one can be set
            currentQuestions.remove(userId);
            userQuestionCount.put(userId, userQuestionCount.getOrDefault(userId, 0) + 1);

            // Get the next question
            ResponseEntity<Map<String, Object>> response = interviewService.nextQuestion(userId);
            Map<String, Object> nextQuestionMap = response.getBody();

            if (nextQuestionMap != null && nextQuestionMap.containsKey("questionId")) {
                currentQuestions.put(userId, ((Number) nextQuestionMap.get("questionId")).longValue());
                questionTimers.put(userId, System.currentTimeMillis());
            }

            return response;
        } catch (ResponseStatusException e) {
            log.error("Error processing response: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing response: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process response", e);
        }
    }

    /**
     * Endpoint to get the next question explicitly if none is active.
     */
    @GetMapping("/askNextQuestion")
    public ResponseEntity<Map<String, Object>> askNextQuestion(@RequestParam Long userId) {
        if (userQuestionCount.getOrDefault(userId, 0) >= 10) {
            return ResponseEntity.ok().body(Collections.singletonMap("message", "You have completed 10 questions. The interview is over."));
        }

        // If no current question, get the next one
        if (!currentQuestions.containsKey(userId)) {
            ResponseEntity<Map<String, Object>> response = interviewService.nextQuestion(userId);
            Map<String, Object> nextQuestionMap = response.getBody();
            if (nextQuestionMap != null && nextQuestionMap.containsKey("questionId")) {
                currentQuestions.put(userId, ((Number) nextQuestionMap.get("questionId")).longValue());
                questionTimers.put(userId, System.currentTimeMillis());
            }
            return response;
        } else {
            // If a question is already active, return it
            Long questionId = currentQuestions.get(userId);
            Question question = questionRepo.findById(questionId).orElse(null);
            if (question != null) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put("questionText", question.getQuestionText());
                questionMap.put("questionId", question.getId());
                return ResponseEntity.ok(questionMap);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Evaluates all answers submitted by a user.
     */
    @GetMapping("/evaluate")
    public List<QuestionEvaluation> evaluateResponses(@RequestParam Long userId) {
        return evaluationService.evaluateUserResponses(userId);
    }

    /**
     * Automatically moves user to next question after 2 minutes of inactivity.
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void checkQuestionTimeouts() {
        log.debug("Checking question timeouts");
        long currentTime = System.currentTimeMillis();
        questionTimers.forEach((userId, startTime) -> {
            if (startTime != null && TimeUnit.MILLISECONDS.toMinutes(currentTime - startTime) >= 2) {
                log.info("Timeout exceeded for user ID: {}. Moving to next question.", userId);
                questionTimers.remove(userId);
                currentQuestions.remove(userId);
                interviewService.nextQuestion(userId);
            }
        });
    }

}
