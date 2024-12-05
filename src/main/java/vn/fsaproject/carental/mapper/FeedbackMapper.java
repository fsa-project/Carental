package vn.fsaproject.carental.mapper;

import org.mapstruct.Mapper;
import vn.fsaproject.carental.dto.request.FeedbackDTO;
import vn.fsaproject.carental.dto.response.FeedbackResponse;
import vn.fsaproject.carental.entities.Feedback;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    Feedback toFeedback(FeedbackDTO request);
    FeedbackResponse toFeedbackResponse(Feedback feedback);
    List<FeedbackResponse> toFeedbackResponseList(List<Feedback> feedbackList);
}
