package vn.fsaproject.carental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.dto.request.FeedbackDTO;
import vn.fsaproject.carental.dto.response.*;
import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.CarImage;
import vn.fsaproject.carental.entities.Feedback;
import vn.fsaproject.carental.mapper.FeedbackMapper;
import vn.fsaproject.carental.repository.FeedbackRepository;
import vn.fsaproject.carental.repository.BookingRepository;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;
    private final FeedbackMapper feedbackMapper;
    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, BookingRepository bookingRepository, FeedbackMapper feedbackMapper) {
        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
        this.feedbackMapper = feedbackMapper;
    }
    public FeedbackResponse createFeedback(Long bookingId, FeedbackDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new RuntimeException(" Invalid booking"));
        Feedback feedback = feedbackMapper.toFeedback(request);
        feedback.setBooking(booking);
        feedback.setDate(LocalDateTime.now());
        feedbackRepository.save(feedback);
        return createFeedbackResponse(feedback);
    }
    private FeedbackResponse createFeedbackResponse(Feedback feedback) {
        Booking booking = feedback.getBooking();
        FeedbackResponse feedbackResponse = feedbackMapper.toFeedbackResponse(feedback);
        feedbackResponse.setUserName(booking.getUser().getName());
        feedbackResponse.setBookingStartDate(booking.getStartDateTime());
        feedbackResponse.setBookingEndDate(booking.getEndDateTime());
        CarImage image = booking.getCar().getImages().get(0);
        String imageUrl = "/api/images/" + Paths.get(image.getFilePath()).getFileName();
        feedbackResponse.setImageUrl(imageUrl);
        return feedbackResponse;
    }
    private FeedbackPaginationResponse createPaginatedResponse(Long carId, Pageable pageable, List<Feedback> feedbacks) {

        List<Feedback> allFeedbacks = feedbackRepository.findByBooking_CarId(carId);

        List<FeedbackResponse> responses = feedbacks.stream()
                .map(this::createFeedbackResponse)
                .toList();
        double averageRating = allFeedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setSize(pageable.getPageSize());
        meta.setPages((int) Math.ceil((double) allFeedbacks.size() / pageable.getPageSize()));
        meta.setTotal(allFeedbacks.size());

        DataPaginationResponse response = new DataPaginationResponse();
        response.setMeta(meta);
        response.setResult(responses);
        FeedbackPaginationResponse paginationResponse = new FeedbackPaginationResponse();
        paginationResponse.setData(response);
        paginationResponse.setAverageRating(averageRating);
        return paginationResponse;
    }
    public FeedbackPaginationResponse getAllFeedbacks(Long carId, Pageable pageable) {
        List<Feedback> feedbacks = feedbackRepository.findByBooking_CarId(carId,pageable);
            return createPaginatedResponse(carId,pageable,feedbacks);
    }


}
