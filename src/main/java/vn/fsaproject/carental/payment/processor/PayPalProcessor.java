package vn.fsaproject.carental.payment.processor;

import org.springframework.stereotype.Component;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.payment.constant.PaymentProcessor;

import java.util.Map;

@Component
public class PayPalProcessor implements PaymentProcessor {
    @Override
    public String generatePaymentUrl(Transaction transaction) {
        // Tạo URL PayPal (logic chi tiết đã trình bày)
        return "https://paypal.com/cgi-bin/webscr";
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {
        // Xác minh callback PayPal (logic chi tiết đã trình bày)
        return null;
    }

    @Override
    public String getType() {
        return "PAYPAL";
    }
}

