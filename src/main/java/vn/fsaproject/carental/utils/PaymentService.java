package vn.fsaproject.carental.utils;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    String createOrder(HttpServletRequest request, int amount, String orderInfo, String returnUrl);
    String getPaymentType();
}
