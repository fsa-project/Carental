package vn.fsaproject.carental.payment.constant;

import vn.fsaproject.carental.entities.Transaction;

import java.util.Map;

public interface PaymentProcessor {
    String generatePaymentUrl(Transaction transaction);

    String handleCallback(Map<String, String> parameters);
}

