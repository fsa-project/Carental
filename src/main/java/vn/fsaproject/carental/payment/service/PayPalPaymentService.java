package vn.fsaproject.carental.payment.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.payment.constant.PaymentService;
import vn.fsaproject.carental.payment.processor.PayPalProcessor;
import vn.fsaproject.carental.service.TransactionService;

import java.util.Map;

@Service("PayPalPaymentService")
public class PayPalPaymentService implements PaymentService {
    private final PayPalProcessor paypalProcessor;
    private final TransactionService transactionService;

    public PayPalPaymentService(PayPalProcessor paypalProcessor, TransactionService transactionService) {
        this.paypalProcessor = paypalProcessor;
        this.transactionService = transactionService;
    }

    @Override
    public String initiatePayment(Transaction transaction) {
        Transaction savedTransaction = transactionService.createTransaction(transaction);
        return paypalProcessor.generatePaymentUrl(savedTransaction);
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {
        String status = paypalProcessor.handleCallback(parameters);
        transactionService.updateTransactionStatus(parameters.get("txn_id"), status);
        return status;
    }
}
