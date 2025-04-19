package com.viarr.viarr_backend.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ATSService {

    private final NLPService nlpService;

    public double calculateScore(String resumeText, String jobDescription) {
        List<String> resumeKeywords = nlpService.extractKeywords(resumeText)
                .stream().map(String::toLowerCase).toList();
        List<String> jobKeywords = nlpService.extractKeywords(jobDescription)
                .stream().map(String::toLowerCase).toList();

        long matches = jobKeywords.stream()
                .filter(jk -> resumeKeywords.stream().anyMatch(rk -> rk.contains(jk) || jk.contains(rk)))
                .count();

        return jobKeywords.isEmpty() ? 0.0 : ((double) matches / jobKeywords.size()) * 100;
    }

}

