package vn.fsaproject.carental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.fsaproject.carental.repository.UserRepository;
import vn.fsaproject.carental.service.EmailService;
import vn.fsaproject.carental.service.OTPService;
import vn.fsaproject.carental.service.UserService;

@RestController
public class ForgotPasswordController {

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/api/forgot_password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        if (!userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is not registered.");
        }
        String otp = otpService.generateOTP(email);
        emailService.sendOTPEmail(email, otp);
        return ResponseEntity.status(HttpStatus.OK).body("OTP sent to your email.");
    }

    @PostMapping("/api/validate_otp")
    public ResponseEntity<?> validateOTP(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.validateOTP(email, otp);
        if (isValid) {
            return ResponseEntity.status(HttpStatus.OK).body("OTP is valid.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP.");
        }
    }

    @PostMapping("/api/reset_password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword, @RequestParam String reEnterPassword) {
        if (!otpService.validateOTP(email, otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP.");
        }
        if (!newPassword.equals(reEnterPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match.");
        }
        userService.updatePassword(email, newPassword);
        return ResponseEntity.status(HttpStatus.OK).body("Password reset successfully. Please log in.");
    }
}