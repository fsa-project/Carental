package vn.fsaproject.carental.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.request.CreateCarDTO;
import vn.fsaproject.carental.dto.request.StartBookingDTO;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.entities.UserBooking;
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

    @PostMapping(value = "/new-booking", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<BookingResponse> createBooking(
            @RequestParam("carId") Long carId,
            @RequestPart("renter") UserBooking renter,
            @RequestPart("driver") UserBooking driver,
            @RequestPart("bookingInfo") StartBookingDTO startBookingDTO) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            BookingResponse response = bookingService.createBooking(userId, carId, startBookingDTO, renter, driver);
            return ResponseEntity.ok(response);
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long bookingId,
            @RequestParam String paymentMethod,
            HttpServletRequest request) {
        try {
            BookingResponse response = bookingService.confirmBooking(bookingId, paymentMethod, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/confirm2/{bookingId}")
    public ResponseEntity<BookingResponse> updatePaymentStatus(@PathVariable Long bookingId,
            @RequestParam String status) {

        try {
            BookingResponse response = bookingService.updateBookingStatus(bookingId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{bookingId}/confirm-pickup")
    public ResponseEntity<BookingResponse> ownerConfirmPickup(
            @PathVariable Long bookingId) {
        Long userId = securityUtil.getCurrentUserId();
        BookingResponse response = bookingService.ownerConfirmPickup(bookingId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete/{bookingId}")
    public ResponseEntity<?> completeBooking(@PathVariable Long bookingId,
                                             @RequestParam String paymentMethod,
                                             HttpServletRequest request
    ) {
        try{
            BookingResponse response = bookingService.completeBooking(bookingId,paymentMethod,request);
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
