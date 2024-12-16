package vn.fsaproject.carental.payment.processor;

import org.springframework.stereotype.Component;
import vn.fsaproject.carental.config.VNPAYConfig;
import vn.fsaproject.carental.constant.TransactionType;
import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.payment.constant.PaymentProcessor;
import vn.fsaproject.carental.repository.TransactionRepository;
import vn.fsaproject.carental.repository.UserRepository;

import java.util.Map;

@Component
public class WalletProcessor implements PaymentProcessor {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    public WalletProcessor(UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }
    @Override
    public String generatePaymentUrl(Transaction transaction) {
        User sender = transaction.getSender();
        String responseCode = "00";
        if (sender.getWallet() < transaction.getAmount()) {
            responseCode = "51";
        }
        return "http://localhost:3000/tkspaying?wallet_Amount=" + (int) transaction.getAmount() + "&wallet_OrderInfo=" + transaction.getBooking().getId() + "&wallet_ResponseCode=" + responseCode + "&wallet_TxnRef=" + transaction.getTransactionId();
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {

        String status = parameters.get("wallet_ResponseCode");
        switch (status) {
            case "00":
                double amount = Double.parseDouble(parameters.get("wallet_Amount"));
                deductWalletBalance(amount, transactionRepository.findByTransactionId(parameters.get("wallet_TxnRef")));
                return "SUCCESS";
            case "24":
                return "CANCELED";
            default:
                return "FAILED";
        }
    }

    @Override
    public String getType() {
        return "WALLET";
    }

    private void deductWalletBalance(double amount, Transaction transaction) {
        if (transaction.getStatus().equalsIgnoreCase("PENDING")) {
            User sender = transaction.getSender();
            User recipient = transaction.getRecipient();

            recipient.setWallet(recipient.getWallet() == 0 ? amount : recipient.getWallet() + amount);
            sender.setWallet(sender.getWallet() - amount);
            userRepository.save(sender);
            userRepository.save(recipient);
        }
    }

}
