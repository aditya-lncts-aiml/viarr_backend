package com.viarr.viarr_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // This is the primary key of Question

    private String questionText;

    private String category;

    @ManyToOne
    @JoinColumn(name = "user_id")  // foreign key column
    private User user; // This creates the relationship

    public Question(Long id, String questionText, String category, User user) {
        this.id = id;
        this.questionText = questionText;
        this.category = category;
        this.user = user;
    }


}
