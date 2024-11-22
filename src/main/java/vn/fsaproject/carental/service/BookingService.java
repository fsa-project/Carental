package vn.fsaproject.carental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.constant.BookingStatus;
import vn.fsaproject.carental.constant.CarStatus;
import vn.fsaproject.carental.dto.request.StartBookingDTO;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.dto.response.Meta;
import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.BookingMapper;
import vn.fsaproject.carental.repository.BookingRepository;
import vn.fsaproject.carental.repository.CarRepository;
import vn.fsaproject.carental.repository.UserRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          CarRepository carRepository,
                          BookingMapper bookingMapper,
                          UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.bookingMapper = bookingMapper;
        this.userRepository = userRepository;
    }

    public BookingResponse createBooking(Long userId, Long carId, StartBookingDTO startBookingDTO) {
        Car car = carRepository.findById(carId).orElseThrow(()-> new RuntimeException("Car not found"));
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        if (!car.getCarStatus().matches(CarStatus.AVAILABLE.getMessage())){
            throw new RuntimeException("Car is not available");
        }
        Booking booking = bookingMapper.toBooking(startBookingDTO);
        booking.setCar(car);
        booking.setUser(user);
        booking.setBookingStatus(BookingStatus.PENDING_DEPOSIT.getMessage());
        bookingRepository.save(booking);
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setId(booking.getId());
        bookingResponse.setBookingStatus(BookingStatus.PENDING_DEPOSIT.getMessage());
        return bookingResponse;
    }



    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new RuntimeException("Booking not found"));
        if (!booking.getBookingStatus().equals(BookingStatus.PENDING_DEPOSIT.getMessage())){
            throw new RuntimeException("Booking is not in a state to be confirm");
        }
        Car car = booking.getCar();
        car.setCarStatus(CarStatus.BOOKED.getMessage());
        carRepository.save(car);
        User user = booking.getUser();
        if (user == null){
            throw new RuntimeException("User's information not found for booking");
        }
        double requireDeposit = car.getDeposit();
        double userDeposit = user.getWallet();
        if (userDeposit < requireDeposit){
            throw new RuntimeException("User's balance not enough");
        }
        user.setWallet(userDeposit - requireDeposit);
        userRepository.save(user);
        booking.setBookingStatus(BookingStatus.CONFIRMED.getMessage());
        bookingRepository.save(booking);
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setBookingStatus(BookingStatus.CONFIRMED.getMessage());
        bookingResponse.setId(booking.getId());
        return bookingResponse;
    }
    public BookingResponse startBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new RuntimeException("Booking not found"));
        if (!booking.getBookingStatus().equals(BookingStatus.CONFIRMED.getMessage())){
            throw new RuntimeException("Booking is not in a state to be started");
        }
        booking.setBookingStatus(BookingStatus.IN_PROGRESS.getMessage());
        bookingRepository.save(booking);

        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setBookingStatus(BookingStatus.IN_PROGRESS.getMessage());
        bookingResponse.setId(booking.getId());
        return bookingResponse;
    }
    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new RuntimeException("Booking not found"));
        if (!booking.getBookingStatus().equals(BookingStatus.IN_PROGRESS.getMessage())){
            throw new RuntimeException("Booking is not in a state to be completed");
        }
        Car car = booking.getCar();
        User user = booking.getUser();
        if (car == null || user == null){
            throw new RuntimeException("Car or User associate with booking is not available");
        }
        double rentalFee = calculateRentalFee(booking);
        double remainFee = rentalFee - car.getDeposit();

        if (user.getWallet() < remainFee){
            throw new RuntimeException("User's balance not enough");
        }
        user.setWallet(user.getWallet() - remainFee);
        userRepository.save(user);
        car.setCarStatus(CarStatus.STOPPED.getMessage());

        booking.setBookingStatus(BookingStatus.COMPLETED.getMessage());
        bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(booking);
    }
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new RuntimeException("Booking not found"));
        if (!booking.getBookingStatus().equals(BookingStatus.PENDING_DEPOSIT.getMessage())
                && !booking.getBookingStatus().equals(BookingStatus.CONFIRMED.getMessage())){
            throw new RuntimeException("Booking cannot be cancelled in this current state");
        }
        Car car = booking.getCar();
        car.setCarStatus(CarStatus.AVAILABLE.getMessage());
        bookingRepository.delete(booking);
    }

    private double calculateRentalFee(Booking booking) {
        LocalDate startDate = booking.getStartDateTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = booking.getEndDateTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        Long rentalDays = ChronoUnit.DAYS.between(startDate,endDate);
        return rentalDays*booking.getCar().getBasePrice();
    }

    public DataPaginationResponse getUserBookings(Long userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserId(userId,pageable);
        List<BookingResponse> bookingResponses = bookingMapper.toBookingResponses(bookings.getContent());
        DataPaginationResponse response = new DataPaginationResponse();
        Meta meta = new Meta();
        meta.setPage(bookings.getNumber()+1);
        meta.setSize(bookings.getSize());
        meta.setPages(bookings.getTotalPages());
        meta.setTotal(bookings.getTotalElements());
        response.setMeta(meta);
        response.setResult(bookingResponses);
        return response;
    }

}
