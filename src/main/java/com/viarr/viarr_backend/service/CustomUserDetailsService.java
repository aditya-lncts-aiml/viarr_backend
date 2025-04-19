package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to fetch user by username first
        Optional<User> userOptional = userRepository.findByUsername(usernameOrEmail);

        // If not found by username, try by email
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(usernameOrEmail);
        }

        // Throw exception if user is still not found
        User user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        // Ensure role is not null (avoid NPE)
        String role = user.getRole() != null ? "ROLE_" + user.getRole() : "ROLE_USER";

        // Return UserDetails implementation for Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    public String getFirstName(String username) {
        return userRepository.findByUsername(username)
                .map(User::getFirstName)
                .orElse("User not found");
    }

    public Long getUserId(String username){
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(0l);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

}