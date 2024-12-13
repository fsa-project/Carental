package vn.fsaproject.carental.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.repository.TransactionRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentProcessor {
    private final List<PaymentService> paymentServices;
    private final TransactionRepository transactionRepository;

    public PaymentProcessor(List<PaymentService> paymentServices, TransactionRepository transactionRepository) {
        this.paymentServices = paymentServices;
        this.transactionRepository = transactionRepository;
    }

    public String processPayment(HttpServletRequest request, String paymentType, int amount, String orderInfo, String returnUrl) {
        PaymentService paymentService = paymentServices.stream()
                .filter(service -> service.getPaymentType().equalsIgnoreCase(paymentType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment type: " + paymentType));

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setPaymentType(paymentType);
        transaction.setAmount((double) amount);
        transaction.setDescription(orderInfo);
        transaction.setStatus("PENDING");
        transactionRepository.save(transaction);



        return paymentService.createOrder(request, amount, orderInfo, returnUrl);
    }
}
