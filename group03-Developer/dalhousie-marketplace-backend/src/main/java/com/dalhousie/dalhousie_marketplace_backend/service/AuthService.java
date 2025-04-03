package com.dalhousie.dalhousie_marketplace_backend.service;

import com.dalhousie.dalhousie_marketplace_backend.model.User;
import com.dalhousie.dalhousie_marketplace_backend.repository.UserRepository;
import com.dalhousie.dalhousie_marketplace_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String appBaseUrl;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private static final int OTP_UPPER_BOUND = 900000;
    private static final int OTP_LOWER_BOUND = 100000;
    private static final int OTP_EXPIRY_MINUTES = 5;


    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String registerUser(User user) {
        Optional<User> emailUser = userRepository.findByEmail(user.getEmail());
        Optional<User> bannerUser = userRepository.findByBannerId(user.getbannerId());

        if (emailUser.isPresent() && emailUser.get().isVerified()) {
            return "Email is already in use. Please use a different email or log in.";
        }

        if (bannerUser.isPresent() && !bannerUser.get().getEmail().equals(user.getEmail())) {
            return "Banner ID is already in use.";
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setIsVerified(false);
        user.setVerificationStatus("Pending");
        user.setAccountStatus("Active");
        user.setCreatedAt(LocalDateTime.now());

        if (emailUser.isPresent()) {
            User existing = emailUser.get();
            user.setUserId(existing.getUserId());
            userRepository.save(user);
        } else {
            userRepository.save(user);
        }

        return "User registered successfully";
    }

    public Optional<User> authenticate(String email) {
        return userRepository.findByEmail(email);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void generateAndSendOtp(User user) {
        String otp = String.valueOf(new Random().nextInt(OTP_UPPER_BOUND) + OTP_LOWER_BOUND);
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);

        String subject = "Email Verification OTP";
        String body = "Your OTP for email verification is:\n" + otp + "\nThis OTP is valid for 5 minutes.";
        sendEmail(user.getEmail(), subject, body);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String username) {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public void sendResetPasswordLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateResetToken(email);
        String resetLink = appBaseUrl + "/reset-password?token=" + token;

        sendEmail(email, "Reset Your Password", "Click the link to reset your password: " + resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        String email = jwtUtil.validateResetToken(token);
        if (email == null) {
            throw new RuntimeException("Invalid or expired token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }
}
