package vn.fsaproject.carental.payment.processor;

import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.payment.constant.PaymentProcessor;

import java.util.Map;

public class WalletProcessor implements PaymentProcessor {
    @Override
    public String generatePaymentUrl(Transaction transaction) {
        return "";
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {
        return "";
    }

    @Override
    public String getType() {
        return "WALLET";
    }

}
