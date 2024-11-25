package vn.fsaproject.carental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.fsaproject.carental.constant.CarStatus;
import vn.fsaproject.carental.dto.request.CreateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.response.CarResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.dto.response.Meta;
import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.CarImage;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.CarMapper;
import vn.fsaproject.carental.repository.BookingRepository;
import vn.fsaproject.carental.repository.CarImageRepository;
import vn.fsaproject.carental.repository.CarRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarService {
    private final String uploadDir;
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarImageRepository carImageRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    public CarService(@Value("${file.upload-dir}") String uploadDir,
                      CarRepository carRepository,
                      CarMapper carMapper,
                      CarImageRepository carImageRepository,
                      UserService userService,
                      BookingRepository bookingRepository
    ) {
        this.uploadDir = uploadDir;
        this.carRepository = carRepository;
        this.carMapper = carMapper;
        this.carImageRepository = carImageRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
    }
    /**
     * Finds available cars for a specified time period, applying optional filters and pagination.
     * This method filters cars that are not booked during the provided time span
     * and returns the available cars in a paginated format.
     *
     * For more details, visit the following link:
     * <a href="https://github.com/turkraft/springfilter">Filter</a>
     *
     * @param startTime,endTime The time that renter want to rent a car.
     * @param pageable,spec {@link Pageable} {@link Specification} The specification object for filter
     * @return {@link DataPaginationResponse} paging format
     */

    public DataPaginationResponse findAvailableCars(LocalDateTime startTime, LocalDateTime endTime, Specification<Car> spec,Pageable pageable) {
        // Fetch all cars
        Page<Car> allCars = carRepository.findAll(spec, pageable);
        List<Car> availableCars = new ArrayList<>();
        Date startDate = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());
        // Iterate through all cars and check their bookings
        for (Car car : allCars.getContent()) {
            boolean isBookedInTimeSpan = false;

            // Fetch bookings for the car
            List<Booking> bookings = bookingRepository.findByCarId(car.getId());

            for (Booking booking : bookings) {
                // Compare the booking's start and end dates with the rental period
                if (booking.getStartDateTime().before(endDate) && booking.getEndDateTime().after(startDate)) {
                    isBookedInTimeSpan = true;
                    break;
                }
            }

            // If no overlap, the car is available
            if (!isBookedInTimeSpan) {
                availableCars.add(car);
            }
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), availableCars.size());
        List<Car> paginatedCars = availableCars.subList(start, end);
        List<CarResponse> responses = new ArrayList<>();
        for (Car car : availableCars) {
            String firstImagePath = null;
            if (!car.getImages().isEmpty()) {
                CarImage firstImage = car.getImages().get(0); // Assuming images are stored in order
                firstImagePath = "/api/images/" + Paths.get(firstImage.getFilePath()).getFileName().toString();
            }
            CarResponse carResponse = carMapper.toCarResponse(car);
            if (firstImagePath != null) {
                carResponse.setImages(List.of(firstImagePath));
            } else {
                carResponse.setImages(new ArrayList<>());
            }
            responses.add(carResponse);
        }
        // Prepare pagination metadata
        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1); // Convert 0-based index to 1-based
        meta.setSize(pageable.getPageSize());
        meta.setPages((int) Math.ceil((double) availableCars.size() / pageable.getPageSize()));
        meta.setTotal(availableCars.size());

        // Prepare the response
        DataPaginationResponse response = new DataPaginationResponse();
        response.setMeta(meta);
        response.setResult(responses);

        return response;

    }
    public CarResponse updateToAvailable(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new RuntimeException("Car not found"));
        if (car.getCarStatus().equalsIgnoreCase(CarStatus.STOPPED.getMessage())) {
            car.setCarStatus(CarStatus.AVAILABLE.getMessage());
        }
        carRepository.save(car);
        CarResponse carResponse = carMapper.toCarResponse(car);
        List<String> images = new ArrayList<>();
        String imagePath = null;
        for (CarImage carImage : car.getImages()) {
            imagePath = "/api/images/" + Paths.get(carImage.getFilePath()).getFileName().toString();
            images.add(imagePath);
        }
        carResponse.setImages(images);
        return carResponse;
    }
    public CarResponse handleCreateCar(CreateCarDTO carDTO, MultipartFile[] file) throws IOException {
        // Lấy id người dùng đang trong phiên đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username: {}",authentication.getName());
        String username = authentication.getName();
        User user = userService.handleGetUserByUsername(username);

        // Map các thông tin từ Request vào 1 car mới
        Car car = carMapper.toCar(carDTO);
        car.setUser(user);
        car.setCarStatus(CarStatus.AVAILABLE.getMessage());
        List<CarImage> carImages = new ArrayList<>();

        for (MultipartFile multipartFile : file) {
            String fileName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, multipartFile.getBytes());

            CarImage carImage = new CarImage();
            carImage.setFilePath(filePath.toString());
            carImage.setCar(car); // Set the association
            carImages.add(carImage);
        }
        car.setImages(carImages);

        // Lưu thông tin xe vào DB
        Car savedCar = carRepository.save(car);

        // Thêm đường dẫn cho ảnh vào CarResponse
        List<String> imagePaths;
        imagePaths = savedCar.getImages().stream()
                .map(carImage -> Paths.get(carImage.getFilePath()).getFileName().toString())
                .collect(Collectors.toList());
        CarResponse carResponse = carMapper.toCarResponse(savedCar);
        carResponse.setImages(imagePaths);
        carResponse.setCarStatus(CarStatus.AVAILABLE.getMessage());
        return carResponse;

    }
    public DataPaginationResponse handleGetCars(Long userId, Pageable pageable){
        // Tạo đối tượng phân trang
        Page<Car> cars = carRepository.findByUserId(userId,pageable);
        // Thêm thông tin vào CarResopnse
        List<CarResponse> carResponses = new ArrayList<>();
        for (Car car : cars.getContent()) {
            String firstImagePath = null;
            if (!car.getImages().isEmpty()) {
                CarImage firstImage = car.getImages().get(0); // Assuming images are stored in order
                firstImagePath = "/api/images/" + Paths.get(firstImage.getFilePath()).getFileName().toString();
            }
            CarResponse carResponse = carMapper.toCarResponse(car);
            if (firstImagePath != null) {
                carResponse.setImages(List.of(firstImagePath));
            } else {
                carResponse.setImages(new ArrayList<>());
            }
            carResponses.add(carResponse);
        }
        // Thêm các thông tin về phân trang
        DataPaginationResponse response = new DataPaginationResponse();
        Meta meta = new Meta();
        meta.setPage(cars.getNumber()+1);
        meta.setSize(cars.getSize());
        meta.setPages(cars.getTotalPages());
        meta.setTotal(cars.getTotalElements());
        response.setMeta(meta);
        response.setResult(carResponses);
        return response;
    }
    public CarResponse handleUpdateCar(
            UpdateCarDTO carDTO,
            MultipartFile[] files,
            Long carId,
            Long userId
    ) throws IOException {
        // Tìm car và user theo id và check xem car có phải của user không
        Car car = carRepository.findById(carId).orElseThrow(() -> new RuntimeException("Car not found"));
        User user = userService.handleUserById(userId);
        if (!user.getCars().contains(car)) {
            throw new RuntimeException("Car not found");
        }
        carMapper.updateCar(car, carDTO);
        // Xóa các ảnh của xe cũ
        List<CarImage> oldImages = carImageRepository.findByCarId(carId);
        for (CarImage oldImage : oldImages) {
            log.info("File path: {}", oldImage.getFilePath());
            Path oldFilePath = Paths.get(oldImage.getFilePath());
            try{
                Files.deleteIfExists(oldFilePath);
            }catch (IOException e){
                throw new RuntimeException("Failed to delete old file:"+oldFilePath);
            }
            carImageRepository.delete(oldImage);
        }
        // thêm lại các ảnh được update
        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.write(filePath, file.getBytes());
            // debug
            log.info("new file path: {}", filePath.toString());
            CarImage carImage = new CarImage();
            carImage.setFilePath(filePath.toString());
            carImage.setCar(car);
            carImageRepository.save(carImage);
        }
        List<CarImage> updatedImages = carImageRepository.findByCarId(carId);

        carRepository.save(car);
        // Trả về đường dẫn cho CarResponse
        CarResponse carResponse = carMapper.toCarResponse(car);
        List<String> imagePaths = updatedImages.stream()
                .map(carImage -> Paths.get(carImage.getFilePath())
                        .getFileName().toString())
                .collect(Collectors.toList());
        carResponse.setImages(imagePaths);
        return carResponse;
    }
    public void handleDeleteCar(Long id) {
        carRepository.deleteById(id);
    }
}
