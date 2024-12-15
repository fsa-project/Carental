package vn.fsaproject.carental.payment.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.payment.constant.PaymentService;

import java.util.Map;

@Component("wallet")
public class WalletPaymentService implements PaymentService {
    @Override
    public String initiatePayment(Transaction transaction) {
        return "";
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {
        return "";
    }
}
