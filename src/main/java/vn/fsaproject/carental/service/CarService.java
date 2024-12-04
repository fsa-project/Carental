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
import vn.fsaproject.carental.dto.response.CarDetailResponse;
import vn.fsaproject.carental.dto.response.CarResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.dto.response.Meta;
import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.CarDocument;
import vn.fsaproject.carental.entities.CarImage;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.CarMapper;
import vn.fsaproject.carental.repository.BookingRepository;
import vn.fsaproject.carental.repository.CarImageRepository;
import vn.fsaproject.carental.repository.CarRepository;
import vn.fsaproject.carental.repository.UserRepository;
import vn.fsaproject.carental.utils.SecurityUtil;

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
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public CarService(
            @Value("${file.upload-dir}") String uploadDir,
            CarRepository carRepository,
            CarMapper carMapper,
            CarImageRepository carImageRepository,
            UserService userService,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            SecurityUtil securityUtil) {
        this.uploadDir = uploadDir;
        this.carRepository = carRepository;
        this.carMapper = carMapper;
        this.carImageRepository = carImageRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    /**
     * Finds available cars for a specified time period with optional filters and
     * pagination.
     *
     * @param startTime Time period start.
     * @param endTime   Time period end.
     * @param spec      Filter specifications.
     * @param pageable  Pagination information.
     * @return Paginated response with available cars.
     */
    public DataPaginationResponse findAvailableCars(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Specification<Car> spec,
            Pageable pageable) {
        Date startDate = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());
        List<Car> allCars = carRepository.findAll().stream()
                .filter(car -> bookingRepository.findByCarId(car.getId())
                        .stream()
                        .noneMatch(booking -> booking.getStartDateTime().before(endDate) &&
                                booking.getEndDateTime().after(startDate)))
                .toList();
        List<Car> availableCars = carRepository.findAll(spec, pageable).stream()
                .filter(car -> bookingRepository.findByCarId(car.getId())
                        .stream()
                        .noneMatch(booking -> booking.getStartDateTime().before(endDate) &&
                                booking.getEndDateTime().after(startDate)))
                .toList();

        // Handle pagination and mapping
        return createPaginatedResponse(pageable, availableCars, allCars);
    }

    public CarResponse updateToAvailable(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        if (CarStatus.STOPPED.getMessage().equalsIgnoreCase(car.getCarStatus())) {
            car.setCarStatus(CarStatus.AVAILABLE.getMessage());
        }
        carRepository.save(car);

        return createCarResponse(car);
    }

    public CarResponse handleCreateCar(CreateCarDTO carDTO, MultipartFile[] documents, MultipartFile[] images)
            throws IOException {
        User user = getCurrentAuthenticatedUser();
        Car car = carMapper.toCar(carDTO);
        car.setUser(user);
        car.setCarStatus(CarStatus.AVAILABLE.getMessage());

        car.setDocuments(saveDocuments(car, documents));
        car.setImages(saveImages(car, images));
        carRepository.save(car);

        return createCarResponse(car);
    }

    public CarDetailResponse handleGetCar(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        return createCarDetailResponse(car);
    }

    public DataPaginationResponse handleGetCars(Long userId, Pageable pageable) {
        List<Car> allCars = carRepository.findByUserId(userId);
        Page<Car> cars = carRepository.findByUserId(userId, pageable);
        return createPaginatedResponse(pageable, cars.getContent(), allCars);
    }

    public CarResponse handleUpdateCar(UpdateCarDTO carDTO, MultipartFile[] images, Long carId, Long userId)
            throws IOException {
        Car car = validateUserCarOwnership(carId, userId);
        carMapper.updateCar(car, carDTO);

        deleteCarImages(carId);
        car.setImages(saveImages(car, images));

        carRepository.save(car);
        return createCarResponse(car);
    }

    public void handleDeleteCar(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        deleteCarImages(carId);
        carRepository.delete(car);
    }

    // Helper methods

    private List<CarDocument> saveDocuments(Car car, MultipartFile[] documents) throws IOException {
        if (documents == null)
            return new ArrayList<>();
        List<CarDocument> carDocuments = new ArrayList<>();
        for (MultipartFile document : documents) {
            String fileName = UUID.randomUUID() + "_" + document.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, "documents", fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, document.getBytes());

            CarDocument carDocument = new CarDocument();
            carDocument.setDocumentName(document.getOriginalFilename());
            carDocument.setFilePath(filePath.toString());
            carDocument.setFileType(document.getContentType());
            carDocument.setCar(car);
            carDocuments.add(carDocument);
        }
        return carDocuments;
    }

    private List<CarImage> saveImages(Car car, MultipartFile[] images) throws IOException {
        List<CarImage> carImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, image.getBytes());

            CarImage carImage = new CarImage();
            carImage.setFilePath(filePath.toString());
            carImage.setCar(car);
            carImages.add(carImage);
        }
        return carImages;
    }

    private void deleteCarImages(Long carId) {
        List<CarImage> images = carImageRepository.findByCarId(carId);
        images.forEach(image -> {
            try {
                Files.deleteIfExists(Paths.get(image.getFilePath()));
            } catch (IOException e) {
                log.error("Failed to delete file: {}", image.getFilePath(), e);
            }
        });
        carImageRepository.deleteAll(images);
    }

    private CarResponse createCarResponse(Car car) {
        CarResponse response = carMapper.toCarResponse(car);
        response.setImages(car.getImages().stream()
                .map(img -> "/api/images/" + Paths.get(img.getFilePath()).getFileName())
                .toList());
        response.setDocuments(car.getDocuments().stream()
                .map(doc -> "/api/documents/" + Paths.get(doc.getFilePath()).getFileName())
                .toList());
        return response;
    }
    private CarDetailResponse createCarDetailResponse(Car car) {
        CarDetailResponse response = carMapper.toCarDetailResponse(car);
        response.setCarStatus(car.getCarStatus());
        response.setId(car.getId());
        response.setImages(car.getImages().stream()
                .map(img -> "/api/images/" + Paths.get(img.getFilePath()).getFileName())
                .toList());
        response.setDocuments(car.getDocuments().stream()
                .map(doc -> "/api/documents/" + Paths.get(doc.getFilePath()).getFileName())
                .toList());
        return response;
    }

    private DataPaginationResponse createPaginatedResponse(Pageable pageable, List<Car> cars, List<Car> allCars) {
        //Long userId = securityUtil.getCurrentUserId();
        //List<Car> allCars = carRepository.findByUserId(userId);


        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setSize(pageable.getPageSize());
        meta.setPages((int) Math.ceil((double) allCars.size() / pageable.getPageSize()));
        meta.setTotal(allCars.size());

        List<CarResponse> responses = cars.stream()
                .map(this::createCarResponse)
                .toList();

        DataPaginationResponse response = new DataPaginationResponse();
        response.setMeta(meta);
        response.setResult(responses);
        return response;
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.handleGetUserByUsername(username);
    }

    private Car validateUserCarOwnership(Long carId, Long userId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        User user = userRepository.findById(carId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getCars().contains(car)) {
            throw new RuntimeException("Car does not belong to user");
        }
        return car;
    }
}
