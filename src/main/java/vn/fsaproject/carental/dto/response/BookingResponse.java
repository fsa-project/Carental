package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.fsaproject.carental.entities.UserBooking;

import java.util.Date;

@Getter
@Setter
public class BookingResponse {
    private Long id;
    private Long carId;
    private UserBooking renter;
    private UserBooking driver;
    private String bookingStatus;
    private Date startDateTime;
    private Date endDateTime;
    private String paymentUrl;
    private double totalAmount;
    private double deposit;
}
