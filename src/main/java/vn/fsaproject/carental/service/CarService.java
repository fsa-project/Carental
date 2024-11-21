package vn.fsaproject.carental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.fsaproject.carental.dto.request.CreateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.response.CarResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.dto.response.Meta;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.CarImage;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.CarMapper;
import vn.fsaproject.carental.repository.CarImageRepository;
import vn.fsaproject.carental.repository.CarRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarService {
    private final String uploadDir;
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarImageRepository carImageRepository;
    private final UserService userService;

    public CarService(@Value("${file.upload-dir}") String uploadDir,
                      CarRepository carRepository,
                      CarMapper carMapper,
                      CarImageRepository carImageRepository,
                      UserService userService
    ) {
        this.uploadDir = uploadDir;
        this.carRepository = carRepository;
        this.carMapper = carMapper;
        this.carImageRepository = carImageRepository;
        this.userService = userService;
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
        CarResponse response = carMapper.toCarResponse(savedCar);
        response.setImages(imagePaths);

        return response;

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
