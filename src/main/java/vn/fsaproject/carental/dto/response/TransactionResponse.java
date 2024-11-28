package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.fsaproject.carental.constant.TransactionType;

import java.time.LocalDateTime;
@Getter
@Setter
public class TransactionResponse {
    private double amount;
    private TransactionType transactionType; // e.g., DEPOSIT, PAYMENT, REFUND
    private LocalDateTime transactionDate;
    private String description;
}
