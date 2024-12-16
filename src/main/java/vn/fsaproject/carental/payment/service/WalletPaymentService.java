package vn.fsaproject.carental.payment.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.payment.constant.PaymentService;
import vn.fsaproject.carental.payment.processor.VNPayProcessor;
import vn.fsaproject.carental.payment.processor.WalletProcessor;
import vn.fsaproject.carental.service.TransactionService;

import java.util.Map;

@Service("WalletPaymentService")
public class WalletPaymentService implements PaymentService {

    private final WalletProcessor walletProcessor;
    private final TransactionService transactionService;

    public WalletPaymentService(WalletProcessor walletProcessor, TransactionService transactionService) {
        this.walletProcessor = walletProcessor;
        this.transactionService = transactionService;
    }

    @Override
    public String initiatePayment(Transaction transaction) {
        Transaction savedTransaction = transactionService.createTransaction(transaction);
        return walletProcessor.generatePaymentUrl(savedTransaction);
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {
        String status = walletProcessor.handleCallback(parameters);
        transactionService.updateTransactionStatus(parameters.get("wallet_TxnRef"), status);
        return status;
    }
}
