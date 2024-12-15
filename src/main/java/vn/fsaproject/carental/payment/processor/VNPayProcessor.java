package vn.fsaproject.carental.payment.processor;
import org.springframework.stereotype.Component;
import vn.fsaproject.carental.config.VNPAYConfig;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.payment.constant.PaymentProcessor;

import java.util.HashMap;
import java.util.Map;

@Component
public class VNPayProcessor implements PaymentProcessor {
    @Override
    public String generatePaymentUrl(Transaction transaction) {
        // Tạo URL VNPay (logic chi tiết đã trình bày)
        return "";
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {
        // Xác minh callback VNPay (logic chi tiết đã trình bày)
        return null;
    }

}
