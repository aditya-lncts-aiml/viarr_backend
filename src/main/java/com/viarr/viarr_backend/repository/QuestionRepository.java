package com.viarr.viarr_backend.repository;

import com.viarr.viarr_backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByUser_Id(Long userId);
    void deleteAllByUserUsername(String username);
    Question findFirstByOrderByIdAsc();
}
