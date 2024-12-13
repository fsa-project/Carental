package vn.fsaproject.carental.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import vn.fsaproject.carental.utils.PaymentService;

@Component("paypal")
public class PayPalPaymentService implements PaymentService {
    @Override
    public String createOrder(HttpServletRequest request, int amount, String orderInfo, String returnUrl) {
        return "";
    }

    @Override
    public String getPaymentType() {
        return "paypal";
    }
}
