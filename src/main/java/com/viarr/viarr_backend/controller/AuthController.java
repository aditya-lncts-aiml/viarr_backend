package com.viarr.viarr_backend.controller;

import com.viarr.viarr_backend.dto.LoginRequest;
import com.viarr.viarr_backend.dto.AuthResponse;
import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.repository.UserRepository;
import com.viarr.viarr_backend.service.CustomUserDetailsService;
import com.viarr.viarr_backend.service.ForgotPasswordService;
import com.viarr.viarr_backend.util.JWTUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ForgotPasswordService forgotPasswordService;


    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already taken!"));
        }

        // Store the raw password temporarily before encoding
        String rawPassword = user.getPassword();

        // Encode and save the user
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepo.save(user);
        // Authenticate with the raw password (before encoding)
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), rawPassword)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(authentication);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "token", token
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Registration successful but automatic login failed"));
        }
    }




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(authentication);

            return ResponseEntity.ok(new AuthResponse("success", token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("error", "Invalid username or password"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("error", "Something went wrong"));
        }
    }




    @GetMapping("/api/users/exists")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userRepo.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        try {
            boolean isAvailable = !userRepo.existsByUsername(username);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error checking username",
                            "available", true // Fallback to true
                    ));
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        String username = jwtUtil.extractUsername(token);
        String firstName = userDetailsService.getFirstName(username);

        if (firstName.equals("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found or has been deleted."));
        }

        return ResponseEntity.ok(Map.of("firstName", firstName));
    }

    @GetMapping("/current-user-id")
    public ResponseEntity<?> getCurrentUserId(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        String username = jwtUtil.extractUsername(token);
        Long UserId = userDetailsService.getUserId(username);

        if (UserId.equals(0l)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "UserId not found or has been deleted."));
        }

        return ResponseEntity.ok(Map.of("userId", UserId));
    }



    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        try {
            boolean isAvailable = !userRepo.existsByEmail(email);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error checking email",
                            "available", true // Fallback to true
                    ));
        }
    }

    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestResetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            forgotPasswordService.generateResetToken(email);
            return ResponseEntity.ok(Map.of("message", "Reset link sent to your email"));
        } catch (Exception e) {
            log.error("Error sending reset email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        try {
            forgotPasswordService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password successfully reset"));
        } catch (Exception e) {
            log.error("Password reset error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        forgotPasswordService.generateResetToken(email);
        return ResponseEntity.ok("Reset link sent successfully");
    }



}
