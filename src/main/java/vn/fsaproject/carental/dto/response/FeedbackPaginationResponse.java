package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackPaginationResponse {
    DataPaginationResponse data;
    double averageRating;
}
