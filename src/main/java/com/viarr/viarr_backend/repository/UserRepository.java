package com.viarr.viarr_backend.repository;

import com.viarr.viarr_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // For login and registration
    boolean existsByUsername(String username); // Already used
    boolean existsByEmail(String email); // For email availability check
}