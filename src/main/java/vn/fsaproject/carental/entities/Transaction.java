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

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = true)
    private Booking booking;

    private double amount;

    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String status;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "transaction_date")
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

}
