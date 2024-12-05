package vn.fsaproject.carental.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int OTP_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    public String generateOTP(String email) {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        String otpStr = otp.toString();
        otpStore.put(email, otpStr);
        return otpStr;
    }

    public boolean validateOTP(String email, String otp) {
        return otp.equals(otpStore.get(email));
    }
}