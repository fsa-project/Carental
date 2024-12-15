package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.constant.TransactionType;
import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySender(User sender);

    List<Transaction> findByBooking(Booking booking);

    List<Transaction> findByStatus(String status);

    List<Transaction> findByTransactionType(TransactionType transactionType);

    Transaction findByTransactionId(String transactionId);

    List<Transaction> findBySenderId(Long senderId);
}
