package vn.fsaproject.carental.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import vn.fsaproject.carental.entities.*;
import vn.fsaproject.carental.mapper.BookingMapper;
import vn.fsaproject.carental.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BookingMapper bookingMapper;
    private final VNPAYService vnpayService;
    private final UserBookingRepository userBookingRepository;

    public BookingService(BookingRepository bookingRepository,
                          CarRepository carRepository,
                          BookingMapper bookingMapper,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          VNPAYService vnpayService,
                          UserBookingRepository userBookingRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.bookingMapper = bookingMapper;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.vnpayService = vnpayService;
        this.userBookingRepository = userBookingRepository;
    }

    // Main booking flow methods
    public BookingResponse createBooking(Long userId, Long carId, StartBookingDTO startBookingDTO, UserBooking renter, UserBooking driver) {
        Car car = findCarById(carId);
        User user = findUserById(userId);
        if (user.getCars().contains(car)) {
            throw new RuntimeException("Can not rent your own car ^.^");
        }
        // Change to validate by renting period
        Date startDateTime = startBookingDTO.getStartDateTime();
        Date endDateTime = startBookingDTO.getEndDateTime();
        validateCarAvailability(car, startDateTime, endDateTime);

        Booking booking = initializeBooking(startBookingDTO, car, user, renter, driver);
        bookingRepository.save(booking);
        return buildBookingResponse(booking, BookingStatus.PENDING_DEPOSIT.getMessage());
    }

    public BookingResponse confirmBooking(Long bookingId, String paymentMethod, HttpServletRequest request) {
        Booking booking = findBookingById(bookingId);
        validateBookingStatus(booking, BookingStatus.PENDING_DEPOSIT);

        String url = processDepositPayment(booking, paymentMethod, request);
        //updateBookingStatus(booking,BookingStatus.DEPOSIT_PAID);
        BookingResponse response = buildBookingResponse(booking, BookingStatus.CONFIRMED.getMessage());
        response.setVnPayUrl(url);
        return response;
    }

    public BookingResponse updateBookingStatus2(String status, Long bookingId) {
        Booking booking = findBookingById(bookingId);
        Car car = booking.getCar();
        User customer = booking.getUser();
        User owner = car.getUser();
        double depositAmount = car.getDeposit();
        int deposit = (int) depositAmount;
        double rentalFee = calculateRentalFee(booking);

        if (Objects.equals(status, "00")) {
            if (booking.getBookingStatus().equals(BookingStatus.PENDING_DEPOSIT.getMessage())) {
                validateBookingStatus(booking, BookingStatus.PENDING_DEPOSIT);
                updateCarStatus(booking.getCar(), CarStatus.BOOKED);
                updateBookingStatus(booking, BookingStatus.DEPOSIT_PAID);

                // Record deposit transaction
                recordTransaction(customer, booking, -depositAmount, TransactionType.DEPOSIT, "Deposit payment processed");
                recordTransaction(owner, booking, depositAmount, TransactionType.DEPOSIT, "Deposit payment received");

                owner.setWallet(owner.getWallet() == null ? depositAmount : owner.getWallet() + depositAmount);
                return buildBookingResponse(booking, BookingStatus.DEPOSIT_PAID.getMessage());
            }

            if (booking.getBookingStatus().equals(BookingStatus.IN_PROGRESS.getMessage())) {
                // Record rental fee transaction
                recordTransaction(customer, booking, -rentalFee, TransactionType.PAYMENT, "Rental fee payment processed");
                recordTransaction(owner, booking, rentalFee, TransactionType.PAYMENT, "Rental fee payment received");

                owner.setWallet(owner.getWallet() + rentalFee);
                updateBookingStatus(booking, BookingStatus.PAYMENT_PAID);

                return buildBookingResponse(booking, BookingStatus.PAYMENT_PAID.getMessage());
            }
            return buildBookingResponse(booking, BookingStatus.PAYMENT_PAID.getMessage());
        }
        return buildBookingResponse(booking, booking.getBookingStatus());
    }

    public BookingResponse ownerConfirmDeposit(Long bookingId, Long ownerId) {
        Booking booking = findBookingById(bookingId);

        validateBookingStatus(booking, BookingStatus.DEPOSIT_PAID);

        updateBookingStatus(booking, BookingStatus.CONFIRMED);

        return buildBookingResponse(booking, BookingStatus.CONFIRMED.getMessage());
    }

    public DataPaginationResponse ownerBookingList(Long ownerId, Pageable pageable) {
        List<Car> cars = carRepository.findByUserId(ownerId);
        List<Booking> bookings = cars.stream()
                .flatMap(car -> bookingRepository.findByCarId(car.getId()).stream())
                .collect(Collectors.toList());
        Page<Booking> pagedBookings = new PageImpl<>(bookings, pageable, bookings.size());
        return buildPaginatedResponse(pagedBookings);
    }

    public BookingResponse ownerConfirmPickup(Long bookingId, Long ownerId) {
        Booking booking = findBookingById(bookingId);

        validateBookingStatus(booking, BookingStatus.CONFIRMED);

        updateBookingStatus(booking, BookingStatus.IN_PROGRESS);

        return buildBookingResponse(booking, BookingStatus.IN_PROGRESS.getMessage());
    }

    public BookingResponse ownerConfirmPayment(Long bookingId, Long ownerId) {
        Booking booking = findBookingById(bookingId);

        validateBookingStatus(booking, BookingStatus.PAYMENT_PAID);

        updateBookingStatus(booking, BookingStatus.COMPLETED);

        updateCarStatus(booking.getCar(), CarStatus.AVAILABLE);

        return buildBookingResponse(booking, BookingStatus.COMPLETED.getMessage());
    }

    /**
     * Change the car status after completing the booking to 'available' instead of 'stopped' as before.
     * The car can still be manually set to 'stopped' using the {@link CarService#updateToStopped} method.
     */
    public BookingResponse paymentPaid(Long bookingId, String paymentMethod, HttpServletRequest request) {
        Booking booking = findBookingById(bookingId);
        Car car = booking.getCar();
        User customer = booking.getUser();
        User owner = car.getUser();
        double depositAmount = car.getDeposit();
//        int deposit = (int) depositAmount;
        double rentalFee = calculateRentalFee(booking);
//        int remainingFee = (int) rentalFee;
        validateBookingStatus(booking, BookingStatus.IN_PROGRESS);
        String url = processRentalPayment(booking, paymentMethod, request);
        //updateBookingStatus(booking, BookingStatus.PAYMENT_PAID);
        BookingResponse response;
        if (paymentMethod.equalsIgnoreCase("wallet")) {
            response = buildBookingResponse(booking, BookingStatus.PAYMENT_PAID.getMessage());
            recordTransaction(customer, booking, -depositAmount, TransactionType.PAYMENT, "Rental payment processed");
            recordTransaction(owner, booking, depositAmount, TransactionType.PAYMENT, "Rental payment processed");
            recordTransaction(owner, booking, -car.getDeposit(), TransactionType.DEPOSIT, "Deposit payment refund");
            recordTransaction(customer, booking, car.getDeposit(), TransactionType.DEPOSIT, "Deposit payment refund");
            customer.setWallet(customer.getWallet() == null ? car.getDeposit() : customer.getWallet() - car.getDeposit());
            owner.setWallet(owner.getWallet() == null ? depositAmount : owner.getWallet() + depositAmount);
            userRepository.save(customer);
            userRepository.save(owner);
        } else {
            response = buildBookingResponse(booking, booking.getBookingStatus());
        }
        response.setVnPayUrl(url);
        return response;
    }

    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        validateCancellableBookingStatus(booking);

        updateBookingStatus(booking, BookingStatus.CANCELED);
        updateCarStatus(booking.getCar(), CarStatus.AVAILABLE);

        return buildBookingResponse(booking, BookingStatus.CANCELED.getMessage());
    }

    public DataPaginationResponse getUserBookings(Long userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageable);
        return buildPaginatedResponse(bookings);
    }

    public BookingResponse getBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        return buildBookingResponse(booking, booking.getBookingStatus());
    }

    // Helper methods
    private void validateCarAvailability(Car car, Date startDateTime, Date endDateTime) {
        // Check if the car's owner stop let this car for rent
        if (car.getCarStatus().equalsIgnoreCase("Stopped")) {
            throw new RuntimeException("Car is currently stopped for renting");
        }
        List<Booking> carBookings = bookingRepository.findByCarId(car.getId());
        for (Booking booking : carBookings) {
            // Check if the car is in middle of an 'in-progress' booking
            if (booking.getBookingStatus().equals(BookingStatus.IN_PROGRESS.getMessage()) &&
                    isOverlapping(booking.getStartDateTime(), booking.getEndDateTime(), startDateTime, endDateTime)) {
                throw new RuntimeException("Car is already booked for the selected period.");
            }
        }
    }

    private boolean isOverlapping(Date existingStart, Date existingEnd, Date newStart, Date newEnd) {
        return newStart.before(existingEnd) && newEnd.after(existingStart);
    }

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

    private void validateBookingStatus(Booking booking, BookingStatus expectedStatus) {
        if (!booking.getBookingStatus().equals(expectedStatus.getMessage())) {
            throw new RuntimeException("Booking is not in a state to be " + expectedStatus.name().toLowerCase());
        }
    }

    private void validateCancellableBookingStatus(Booking booking) {
        if (!booking.getBookingStatus().equalsIgnoreCase(BookingStatus.PENDING_DEPOSIT.getMessage()) &&
                !booking.getBookingStatus().equalsIgnoreCase(BookingStatus.CONFIRMED.getMessage())) {
            throw new RuntimeException("Booking cannot be cancelled in this current state");
        }
    }

    private Booking initializeBooking(StartBookingDTO dto, Car car, User user, UserBooking renter, UserBooking driver) {
        Booking booking = bookingMapper.toBooking(dto);
        userBookingRepository.save(renter);
        userBookingRepository.save(driver);
        booking.setCar(car);
        booking.setUser(user);
        booking.setRenter(renter);
        booking.setDriver(driver);
        booking.setBookingStatus(BookingStatus.PENDING_DEPOSIT.getMessage());
        return booking;
    }

    private String processDepositPayment(Booking booking, String paymentMethod, HttpServletRequest request) {
        String url = "";
        User customer = booking.getUser();
        Car car = booking.getCar();
        double depositAmount = car.getDeposit();
        int deposit = (int) depositAmount;


        if (paymentMethod.equalsIgnoreCase("WALLET")) {
            if (customer.getWallet() < depositAmount) {
                throw new RuntimeException("Insufficient wallet balance for deposit");
            }
            deductWalletBalance(customer, booking, depositAmount, TransactionType.DEPOSIT, "Deposit payment processed");
            booking.setPaymentMethod(paymentMethod);
        }
        if (paymentMethod.equalsIgnoreCase("VNPAY")) {
            url = vnpayService.createOrder(request, deposit, "" + booking.getId(), VNPAYConfig.vnp_Returnurl);

            booking.setPaymentMethod(paymentMethod);
        }
        return url;
    }

    private String processRentalPayment(Booking booking, String paymentMethod, HttpServletRequest request) {
        String url = "";
        User customer = booking.getUser();
        Car car = booking.getCar();
        User owner = car.getUser();
        double rentalFee = calculateRentalFee(booking);
        int remainingFee = (int) rentalFee;

        if (paymentMethod.equalsIgnoreCase("WALLET")) {
            if (customer.getWallet() < rentalFee) {
                throw new RuntimeException("Insufficient wallet balance for rental payment");
            }
            deductWalletBalance(customer, booking, rentalFee, TransactionType.PAYMENT, "Rental payment processed");
            refundWalletBalance(customer, booking, car.getDeposit(), TransactionType.REFUND, "Deposit refunded");
            booking.setPaymentMethod(paymentMethod);
        }
        if (paymentMethod.equalsIgnoreCase("VNPAY")) {
            url = vnpayService.createOrder(request, remainingFee, "" + booking.getId(), VNPAYConfig.vnp_Returnurl);
        }
        return url;
    }

    private void deductWalletBalance(User customer, Booking booking, double amount, TransactionType type, String description) {
        customer.setWallet(customer.getWallet() - amount);
        User owner = booking.getCar().getUser();
        owner.setWallet(owner.getWallet() + amount);
        userRepository.save(owner);
        userRepository.save(customer);
        recordTransaction(customer, booking, -amount, type, description);
        recordTransaction(owner, booking, amount, type, description);
    }

    private void refundWalletBalance(User customer, Booking booking, double amount, TransactionType type, String description) {
        customer.setWallet(customer.getWallet() + amount);
        User owner = booking.getCar().getUser();
        owner.setWallet(owner.getWallet() - amount);
        userRepository.save(owner);
        userRepository.save(customer);
        recordTransaction(owner, booking, -amount, type, description);
        recordTransaction(customer, booking, amount, type, description);
    }

    private void recordTransaction(User user, Booking booking, double amount, TransactionType type, String description) {
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

    private BookingResponse buildBookingResponse(Booking booking, String status) {
        BookingResponse response = bookingMapper.toBookingResponse(booking);
        response.setBookingStatus(status);
        response.setCarId(booking.getCar().getId());
        response.setRenter(booking.getRenter());
        response.setDriver(booking.getDriver());
        response.setId(booking.getId());
        return response;
    }

    private DataPaginationResponse buildPaginatedResponse(Page<Booking> bookings) {
        List<BookingResponse> responses = bookings.getContent().stream()
                .map(booking -> buildBookingResponse(booking, booking.getBookingStatus()))
                .collect(Collectors.toList());
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
