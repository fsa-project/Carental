package vn.fsaproject.carental.payment.constant;

import jakarta.servlet.http.HttpServletRequest;
import vn.fsaproject.carental.entities.Transaction;

import java.util.Map;

public interface PaymentService {
    String initiatePayment(Transaction transaction);
    String handleCallback(Map<String, String> parameters);
}
