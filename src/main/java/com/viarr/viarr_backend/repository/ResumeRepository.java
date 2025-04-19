package com.viarr.viarr_backend.repository;

import com.viarr.viarr_backend.model.Resume;
import com.viarr.viarr_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findTopByUserOrderByIdDesc(User user);
    Optional<Resume> findByUserUsername(String username);
    void deleteAllByUserUsername(String username);
    Resume findByUser(User user);
    Resume findByUserId(Long userId);
}
