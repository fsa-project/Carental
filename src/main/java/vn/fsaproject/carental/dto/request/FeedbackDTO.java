package vn.fsaproject.carental.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackDTO {
    private int rating;
    private String content;
}
