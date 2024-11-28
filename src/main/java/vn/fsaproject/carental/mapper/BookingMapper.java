package vn.fsaproject.carental.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import vn.fsaproject.carental.dto.request.StartBookingDTO;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.entities.Booking;

import java.util.List;

@Component
@org.mapstruct.Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking toBooking(StartBookingDTO startBookingDTO);
    BookingResponse toBookingResponse(Booking booking);
    List<BookingResponse> toBookingResponses(List<Booking> bookings);
}
