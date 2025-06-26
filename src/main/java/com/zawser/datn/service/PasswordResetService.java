package com.zawser.datn.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zawser.datn.entity.User;
import com.zawser.datn.exception.AppException;
import com.zawser.datn.exception.ErrorCode;
import com.zawser.datn.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender mailSender;

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = generateResetToken();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        sendResetEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository
                .findByResetPasswordToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY_EXCEPTION));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_KEY_EXCEPTION);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    private void sendResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText(
                "To reset your password, click the link below:\n\n" + "http://localhost:5173/forgot?token=" + token);

        mailSender.send(message);
    }
}
