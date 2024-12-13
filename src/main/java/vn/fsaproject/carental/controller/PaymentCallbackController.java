package vn.fsaproject.carental.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.repository.TransactionRepository;

@RestController
@RequestMapping("/payment")
public class PaymentCallbackController {

    private final TransactionRepository transactionRepository;

    public PaymentCallbackController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/call-back")
    public String handleCallback(@RequestParam String transactionId,
                                 @RequestParam String status) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId);

        transaction.setStatus(status);
        transactionRepository.save(transaction);

        return "Transaction updated";
    }
}
