package com.viarr.viarr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ResumeAnalysisResult {
    private List<String> skills;
    private List<String> education;
    private List<String> certifications;
    private List<String> accomplishments;
}