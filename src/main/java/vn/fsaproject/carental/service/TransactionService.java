package vn.fsaproject.carental.service;

import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.repository.TransactionRepository;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void updateTransactionStatus(String vnpTxnRef, String status) {

    }
}
