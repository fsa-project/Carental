package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class BookingResponse {
    private Long id;
    private String status;
    private Date startDateTime;
    private Date endDateTime;
}
