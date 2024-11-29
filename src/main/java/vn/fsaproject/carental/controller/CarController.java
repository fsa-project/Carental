package vn.fsaproject.carental.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.fsaproject.carental.dto.request.CreateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.response.CarDetailResponse;
import vn.fsaproject.carental.dto.response.CarResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.repository.CarRepository;
import vn.fsaproject.carental.service.CarService;
import vn.fsaproject.carental.utils.SecurityUtil;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;
    private final SecurityUtil securityUtil;

    public CarController(CarService carService, SecurityUtil securityUtil) {
        this.carService = carService;
        this.securityUtil = securityUtil;
    }

    @ApiMessage("Car create successfully!!!")
    @PostMapping(value = "/create", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<CarResponse> createCar(
            @RequestPart("metadata") CreateCarDTO carDTO, // application/jason
            @RequestParam(value = "documents", required = false) MultipartFile[] documents,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            CarResponse carResponse = carService.handleCreateCar(carDTO, documents, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(carResponse);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiMessage("User's cars")
    @GetMapping("/user-cars")
    public ResponseEntity<DataPaginationResponse> getUserCars(
            Pageable pageable) {
        try {
            Long userID = securityUtil.getCurrentUserId();
            DataPaginationResponse response = carService.handleGetCars(userID, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/user-cars/{carId}")
    public ResponseEntity<CarDetailResponse> getUserCar(@PathVariable("carId") Long id) {
        try {
            CarDetailResponse carResponse = carService.handleGetCar(id);
            return ResponseEntity.ok(carResponse);
        } catch (RuntimeException exception) {
            return ResponseEntity.status(500).build();
        }
    }

    @ApiMessage("Car update successfully!!!")
    @PutMapping(value = "/update/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarResponse> updateCar(
            @RequestPart("metadata") UpdateCarDTO carDTO,
            @PathVariable("carId") Long carId,
            @RequestParam("images") MultipartFile[] images) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            CarResponse carResponse = carService.handleUpdateCar(carDTO, images, carId, userId);

            return ResponseEntity.ok(carResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<DataPaginationResponse> getAvailableOrBookedCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,
            @RequestParam(required = false) String address,
            Pageable pageable) {
        Specification<Car> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction(); // Default to no filtering
            // Filter by address if provided
            if (address != null) {
                predicate = cb.and(predicate, cb.like(root.get("address"), "%" + address + "%"));
            }

            return predicate;
        };
        LocalDateTime startTime = startDate.atStartOfDay(); // Start of the day (00:00:00)
        LocalDateTime endTime = endDate.atTime(23, 59, 59); // End of the day (23:59:59)
        DataPaginationResponse cars = carService.findAvailableCars(startTime, endTime, spec, pageable);
        return ResponseEntity.ok(cars);
    }

    @PutMapping("/activate-car/{id}")
    public ResponseEntity<CarResponse> activateCar(@PathVariable Long id) {
        CarResponse carResponse = carService.updateToAvailable(id);
        return ResponseEntity.ok(carResponse);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable("id") Long id) {
        this.carService.handleDeleteCar(id);
        return ResponseEntity.noContent().build();
    }

}
