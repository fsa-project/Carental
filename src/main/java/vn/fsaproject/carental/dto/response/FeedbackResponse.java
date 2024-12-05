package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class FeedbackResponse {
    private String userName;
    private int rating;
    private String content;
    private LocalDateTime date;
    private Date bookingStartDate;
    private Date bookingEndDate;
    private String imageUrl;
}
