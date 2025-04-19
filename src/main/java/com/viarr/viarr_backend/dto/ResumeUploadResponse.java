package com.viarr.viarr_backend.dto;

import com.viarr.viarr_backend.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ResumeUploadResponse {
    private String message;
    private Map<String, List<String>> extracted;
    private List<Question> questions;
}
