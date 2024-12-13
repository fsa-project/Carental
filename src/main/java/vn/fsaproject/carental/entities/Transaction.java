package vn.fsaproject.carental.entities;

import jakarta.persistence.*;
import lombok.Data;
import vn.fsaproject.carental.constant.PaymentType;
import vn.fsaproject.carental.constant.TransactionType;

import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Foreign key for user
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = true) // Foreign key for booking (nullable for non-booking transactions)
    private Booking booking;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // e.g., DEPOSIT, PAYMENT, REFUND

    private String paymentType;

    private String transactionId;

    private LocalDateTime transactionDate;

    private String status;

    private String description;
}
