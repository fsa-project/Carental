package vn.fsaproject.carental.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public class StartBookingDTO {
    private Date startDateTime;
    private Date endDateTime;
    private String driversInformation;
    private String paymentMethod;
}
