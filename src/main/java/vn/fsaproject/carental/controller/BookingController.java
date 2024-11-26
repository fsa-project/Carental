package vn.fsaproject.carental.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.request.StartBookingDTO;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.service.BookingService;
import vn.fsaproject.carental.utils.SecurityUtil;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final SecurityUtil securityUtil;

    public BookingController(BookingService bookingService, SecurityUtil securityUtil) {
        this.bookingService = bookingService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/new-booking")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestParam("carId") Long carId,
            @RequestBody StartBookingDTO startBookingDTO
            ){
        try{
            Long userId = securityUtil.getCurrentUserId();
            BookingResponse response = bookingService.createBooking(userId, carId, startBookingDTO);
            return ResponseEntity.ok(response);
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long bookingId,
            @RequestParam String paymentMethod
    ) {
        try {
            BookingResponse response = bookingService.confirmBooking(bookingId, paymentMethod);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/start/{bookingId}")
    public ResponseEntity<BookingResponse> startBooking(@PathVariable Long bookingId) {
        try {
            BookingResponse response = bookingService.startBooking(bookingId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/complete/{bookingId}")
    public ResponseEntity<?> completeBooking(@PathVariable Long bookingId,
                                             @RequestParam String paymentMethod
    ) {
        try{
            BookingResponse response = bookingService.completeBooking(bookingId,paymentMethod);
            return ResponseEntity.ok(response);
        }catch (RuntimeException e) {
            log.error("Error completing booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/cancel/{bookingId}")
    @ApiMessage("User has stop booking")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            BookingResponse response = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(response);
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all-booking")
    public ResponseEntity<DataPaginationResponse> getAllBookings(Pageable pageable) {
        Long userId = securityUtil.getCurrentUserId();
        DataPaginationResponse response = bookingService.getUserBookings(userId,pageable);
        return ResponseEntity.ok(response);
    }

}
