package vn.fsaproject.carental.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.repository.TransactionRepository;
import vn.fsaproject.carental.service.BookingService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentCallbackController {

    private final TransactionRepository transactionRepository;
    private final BookingService bookingService;

    public PaymentCallbackController(TransactionRepository transactionRepository,
                                     BookingService bookingService) {
        this.transactionRepository = transactionRepository;
        this.bookingService = bookingService;
    }

    @PostMapping("/call-back/{bookingId}")
    public ResponseEntity<BookingResponse> handleCallback(@PathVariable Long bookingId,
                                                          @RequestBody Map<String, String> params) {

//        Transaction transaction = transactionRepository.findByTransactionId(transactionId);
//
//        transaction.setStatus(status);
//        transactionRepository.save(transaction);
        try {
            if (params.containsKey("vnp_TmnCode")) {
                BookingResponse response = bookingService.callbackProcess(bookingId, params);
                return ResponseEntity.ok(response);
            } else if (params.containsKey("txn_id")) {
                // Paypal call back
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
