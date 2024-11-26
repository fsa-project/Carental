package vn.fsaproject.carental.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.request.StartBookingDTO;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.service.BookingService;
import vn.fsaproject.carental.utils.SecurityUtil;

import java.util.List;

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
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId) {
        try {
            BookingResponse response = bookingService.confirmBooking(bookingId);
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
    @GetMapping("/all-booking")
    public ResponseEntity<DataPaginationResponse> getAllBookings(Pageable pageable) {
        Long userId = securityUtil.getCurrentUserId();
        DataPaginationResponse response = bookingService.getUserBookings(userId,pageable);
        return ResponseEntity.ok(response);
    }
}
