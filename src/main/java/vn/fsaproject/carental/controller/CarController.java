package vn.fsaproject.carental.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.fsaproject.carental.dto.request.CreateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.response.CarResponse;
import vn.fsaproject.carental.dto.response.DataPaginationResponse;
import vn.fsaproject.carental.service.CarService;
import vn.fsaproject.carental.utils.SecurityUtil;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

import java.io.IOException;

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
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarResponse> createCar(
            @RequestPart("metadata") CreateCarDTO carDTO, // application/jason
            @RequestParam("documents") MultipartFile[] documents,
            @RequestParam("images") MultipartFile[] images) {
        try {
            CarResponse carResponse = carService.handleCreateCar(carDTO, documents, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(carResponse);
        } catch (IOException e) {
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

    @GetMapping("/car-detail/{id}")
    public ResponseEntity<CarResponse> getUserCar(@PathVariable("id") Long id) {
        try {
            CarResponse carResponse = carService.handleGetCar(id);
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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable("id") Long id) {
        this.carService.handleDeleteCar(id);
        return ResponseEntity.noContent().build();
    }

}
