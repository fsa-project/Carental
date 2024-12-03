package vn.fsaproject.carental.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.config.VNPAYConfig;
import vn.fsaproject.carental.constant.BookingStatus;
import vn.fsaproject.carental.constant.CarStatus;
import vn.fsaproject.carental.constant.TransactionType;
import vn.fsaproject.carental.dto.request.StartBookingDTO;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.dto.response.Meta;
import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.BookingMapper;
import vn.fsaproject.carental.repository.BookingRepository;
import vn.fsaproject.carental.repository.CarRepository;
import vn.fsaproject.carental.repository.TransactionRepository;
import vn.fsaproject.carental.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BookingMapper bookingMapper;
    private final VNPAYService vnpayService;

    public BookingService(BookingRepository bookingRepository,
                          CarRepository carRepository,
                          BookingMapper bookingMapper,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          VNPAYService vnpayService
                          ) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.bookingMapper = bookingMapper;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.vnpayService = vnpayService;
    }

    // Main booking flow methods
    public BookingResponse createBooking(Long userId, Long carId, StartBookingDTO startBookingDTO) {
        Car car = findCarById(carId);
        User user = findUserById(userId);
        if (user.getCars().contains(car)){
            throw new RuntimeException("Can not rent your own car ^.^");
        }
        validateCarAvailability(car);

        Booking booking = initializeBooking(startBookingDTO, car, user);
        bookingRepository.save(booking);

        return buildBookingResponse(booking, BookingStatus.PENDING_DEPOSIT);
    }

    public BookingResponse confirmBooking(Long bookingId, String paymentMethod, HttpServletRequest request) {
        Booking booking = findBookingById(bookingId);
        validateBookingStatus(booking, BookingStatus.PENDING_DEPOSIT);

        updateBookingStatus(booking,BookingStatus.AWAITING_PICKUP_CONFIRMATION);
        String url = processDepositPayment(booking, paymentMethod, request);
        updateCarStatus(booking.getCar(), CarStatus.BOOKED);
        BookingResponse response = buildBookingResponse(booking, BookingStatus.AWAITING_PICKUP_CONFIRMATION);
        response.setVnPayUrl(url);
        return response;
    }
    public BookingResponse ownerConfirmPickup(Long bookingId, Long ownerId) {
        Booking booking = findBookingById(bookingId);

        // Ensure the owner is the car owner
        if (!booking.getCar().getUser().getId().equals(ownerId)) {
            throw new RuntimeException("Only the car owner can confirm pickup.");
        }

        validateBookingStatus(booking, BookingStatus.AWAITING_PICKUP_CONFIRMATION);

        updateBookingStatus(booking, BookingStatus.IN_PROGRESS);

        return buildBookingResponse(booking, BookingStatus.IN_PROGRESS);
    }


    public BookingResponse startBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        validateBookingStatus(booking, BookingStatus.IN_PROGRESS);

        updateBookingStatus(booking, BookingStatus.IN_PROGRESS);

        return buildBookingResponse(booking, BookingStatus.IN_PROGRESS);
    }

    public BookingResponse completeBooking(Long bookingId, String paymentMethod, HttpServletRequest request) {
        Booking booking = findBookingById(bookingId);
        validateBookingStatus(booking, BookingStatus.IN_PROGRESS);

        processRentalPayment(booking, paymentMethod, request);
        updateBookingStatus(booking, BookingStatus.COMPLETED);
        updateCarStatus(booking.getCar(), CarStatus.STOPPED);

        return buildBookingResponse(booking, BookingStatus.COMPLETED);
    }

    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        validateCancellableBookingStatus(booking);

        updateBookingStatus(booking, BookingStatus.CANCELED);
        updateCarStatus(booking.getCar(), CarStatus.AVAILABLE);

        return buildBookingResponse(booking, BookingStatus.CANCELED);
    }

    public DataPaginationResponse getUserBookings(Long userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageable);
        return buildPaginatedResponse(bookings);
    }

    // Helper methods
    private Car findCarById(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    private void validateCarAvailability(Car car) {
        if (!car.getCarStatus().equals(CarStatus.AVAILABLE.getMessage())) {
            throw new RuntimeException("Car is not available");
        }
    }

    private void validateBookingStatus(Booking booking, BookingStatus expectedStatus) {
        if (!booking.getBookingStatus().equals(expectedStatus.getMessage())) {
            throw new RuntimeException("Booking is not in a state to be " + expectedStatus.name().toLowerCase());
        }
    }

    private void validateCancellableBookingStatus(Booking booking) {
        if (!booking.getBookingStatus().equals(BookingStatus.PENDING_DEPOSIT.getMessage()) &&
                !booking.getBookingStatus().equals(BookingStatus.CONFIRMED.getMessage())) {
            throw new RuntimeException("Booking cannot be cancelled in this current state");
        }
    }

    private Booking initializeBooking(StartBookingDTO dto, Car car, User user) {
        Booking booking = bookingMapper.toBooking(dto);
        booking.setCar(car);
        booking.setUser(user);
        booking.setBookingStatus(BookingStatus.PENDING_DEPOSIT.getMessage());
        return booking;
    }

    private String processDepositPayment(Booking booking, String paymentMethod, HttpServletRequest request) {
        String url = "";
        User user = booking.getUser();
        Car car = booking.getCar();
        double depositAmount = car.getDeposit();
        int deposit = (int) depositAmount;
        if (user.getWallet() < depositAmount) {
            throw new RuntimeException("Insufficient wallet balance for deposit");
        }

        if (paymentMethod.equalsIgnoreCase("WALLET")) {
            deductWalletBalance(user,booking, depositAmount, TransactionType.DEPOSIT, "Deposit payment processed");
            booking.setPaymentMethod(paymentMethod);
        }
        if (paymentMethod.equalsIgnoreCase("VNPAY")) {
            url =  vnpayService.createOrder(request,deposit,"Thanh toan don hang:"+VNPAYConfig.getRandomNumber(8),VNPAYConfig.vnp_Returnurl);
            booking.setPaymentMethod(paymentMethod);
            deductWalletBalance(user,booking,depositAmount, TransactionType.DEPOSIT, "Deposit payment processed");
        }
        return url;
    }

    private String processRentalPayment(Booking booking, String paymentMethod, HttpServletRequest request) {
        String url = "";
        User user = booking.getUser();
        Car car = booking.getCar();
        double rentalFee = calculateRentalFee(booking);
        double remainingFeeAmount = rentalFee - car.getDeposit();
        int remainingFee = (int) remainingFeeAmount;
        if (user.getWallet() < remainingFeeAmount) {
            throw new RuntimeException("Insufficient wallet balance for rental payment");
        }

        if (paymentMethod.equalsIgnoreCase("WALLET")) {
            deductWalletBalance(user,booking, remainingFeeAmount, TransactionType.PAYMENT, "Rental payment processed");
            refundWalletBalance(user,booking, car.getDeposit(), TransactionType.REFUND, "Deposit refunded");
            booking.setPaymentMethod(paymentMethod);
        }
        if (paymentMethod.equalsIgnoreCase("VNPAY")) {
            url =  vnpayService.createOrder(request,remainingFee,"Thanh toan don hang:"+VNPAYConfig.getRandomNumber(8),VNPAYConfig.vnp_Returnurl);
            booking.setPaymentMethod(paymentMethod);
            deductWalletBalance(user,booking,remainingFeeAmount, TransactionType.PAYMENT, "Deposit payment processed");
        }
        return url;

    }

    private void deductWalletBalance(User customer,Booking booking, double amount, TransactionType type, String description) {
        customer.setWallet(customer.getWallet() - amount);
        User owner = booking.getCar().getUser();
        owner.setWallet(owner.getWallet() + amount);
        userRepository.save(owner);
        userRepository.save(customer);
        recordTransaction(customer,booking, -amount, type, description);
        recordTransaction(owner,booking, amount, type, description);
    }

    private void refundWalletBalance(User customer,Booking booking, double amount, TransactionType type, String description) {
        customer.setWallet(customer.getWallet() + amount);
        User owner = booking.getCar().getUser();
        owner.setWallet(owner.getWallet() - amount);
        userRepository.save(owner);
        userRepository.save(customer);
        recordTransaction(owner,booking, -amount, type, description);
        recordTransaction(customer,booking, amount, type, description);
    }

    private void recordTransaction(User user,Booking booking, double amount, TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBooking(booking);
        transactionRepository.save(transaction);
    }

    private void updateCarStatus(Car car, CarStatus status) {
        car.setCarStatus(status.getMessage());
        carRepository.save(car);
    }

    private void updateBookingStatus(Booking booking, BookingStatus status) {
        booking.setBookingStatus(status.getMessage());
        bookingRepository.save(booking);
    }

    private BookingResponse buildBookingResponse(Booking booking, BookingStatus status) {
        BookingResponse response = bookingMapper.toBookingResponse(booking);
        response.setBookingStatus(status.getMessage());
        response.setId(booking.getId());
        return response;
    }

    private DataPaginationResponse buildPaginatedResponse(Page<Booking> bookings) {
        List<BookingResponse> responses = bookingMapper.toBookingResponses(bookings.getContent());
        Meta meta = new Meta();
        meta.setPage(bookings.getNumber() + 1);
        meta.setSize(bookings.getSize());
        meta.setPages(bookings.getTotalPages());
        meta.setTotal(bookings.getTotalElements());

        DataPaginationResponse response = new DataPaginationResponse();
        response.setMeta(meta);
        response.setResult(responses);
        return response;
    }

    private double calculateRentalFee(Booking booking) {
        LocalDate startDate = booking.getStartDateTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = booking.getEndDateTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);
        return rentalDays * booking.getCar().getBasePrice();
    }
}
