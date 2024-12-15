package vn.fsaproject.carental.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import vn.fsaproject.carental.constant.PaymentType;
import vn.fsaproject.carental.constant.TransactionType;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = true)
    private Booking booking;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private String status;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "payment_reference_id")
    private String paymentReferenceId;

    @Column(name = "secure_hash")
    private String secureHash;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "return_url")
    private String returnUrl;

    @Column(length = 500)
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "locale")
    private String locale = "vn";

    @Column(name = "fee")
    private Double fee;

    @Column(name = "net_amount")
    private Double netAmount;

    @Column(name = "is_test")
    private Boolean isTest = false;

    @Column(name = "refund_transaction_id")
    private Long refundTransactionId;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
}
