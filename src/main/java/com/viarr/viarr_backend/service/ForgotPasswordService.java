package com.viarr.viarr_backend.service;

import com.viarr.viarr_backend.model.PasswordResetToken;
import com.viarr.viarr_backend.model.User;
import com.viarr.viarr_backend.repository.PasswordResetTokenRepository;
import com.viarr.viarr_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 🚨 Delete existing token if present
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);

        tokenRepository.save(resetToken);

        sendResetEmail(email, token);
    }


    private void sendResetEmail(String email, String token) {
        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ishusinha919@gmail.com"); // ✅ Set From Address explicitly
        message.setTo(email);
        message.setSubject("🔐 Reset Your Password");
        message.setText("Hello,\n\nClick the link below to reset your password:\n" + resetLink +
                "\n\nThis link will expire in 30 minutes.\n\nRegards,\nVIARR Team");

        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Optional: delete the token after successful reset
        tokenRepository.delete(resetToken);
    }

}
