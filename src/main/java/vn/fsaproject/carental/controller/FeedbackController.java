package vn.fsaproject.carental.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.request.FeedbackDTO;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.dto.response.FeedbackPaginationResponse;
import vn.fsaproject.carental.dto.response.FeedbackResponse;
import vn.fsaproject.carental.entities.Feedback;
import vn.fsaproject.carental.service.FeedbackService;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/create-feedback")
    public FeedbackResponse createFeedback(
            @RequestParam("bookingId") Long bookingId,
            @RequestBody FeedbackDTO request) {
        return feedbackService.createFeedback(bookingId, request);
    }
    @GetMapping("/car-report")
    public ResponseEntity<FeedbackPaginationResponse> getCarReports(
            @RequestParam("carId") Long carId,
            Pageable pageable
    ){
        try {
            FeedbackPaginationResponse response = feedbackService.getAllFeedbacks(carId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
