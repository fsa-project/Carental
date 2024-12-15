package vn.fsaproject.carental.payment.service;

import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.payment.constant.PaymentService;
import vn.fsaproject.carental.payment.processor.VNPayProcessor;
import vn.fsaproject.carental.service.TransactionService;

import java.util.*;

@Service("VNPayPaymentService")
public class VNPayPaymentService implements PaymentService {

    private final VNPayProcessor vnpayProcessor;
    private final TransactionService transactionService;

    public VNPayPaymentService(VNPayProcessor vnpayProcessor, TransactionService transactionService) {
        this.vnpayProcessor = vnpayProcessor;
        this.transactionService = transactionService;
    }

    @Override
    public String initiatePayment(Transaction transaction) {
        Transaction savedTransaction = transactionService.createTransaction(transaction);
        return vnpayProcessor.generatePaymentUrl(savedTransaction);
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {
        String status = vnpayProcessor.handleCallback(parameters);
        transactionService.updateTransactionStatus(parameters.get("vnp_TxnRef"), status);
        return status;
    }
}