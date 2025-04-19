package com.viarr.viarr_backend.repository;

import com.viarr.viarr_backend.model.InterviewResponse;
import com.viarr.viarr_backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InterviewResponseRepository extends JpaRepository<InterviewResponse, Long> {

    // Fetch all interview responses for a specific user
    List<InterviewResponse> findByUserId(Long userId);

    // Delete all interview responses for a specific user
    @Modifying
    @Transactional
    void deleteAllByUserId(@Param("userId") Long userId);

    // Check if a user has responded to a specific question
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);

}
