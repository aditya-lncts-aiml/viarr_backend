package com.viarr.viarr_backend.model;


import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
public class InterviewResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)  // Foreign key to User
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)  // Foreign key to Question
    private Question question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    private Integer score;
    private String feedback;

    @CreationTimestamp
    private Timestamp timestamp;
}
