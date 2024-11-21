package vn.fsaproject.carental.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.service.CarService;
import vn.fsaproject.carental.utils.SecurityUtil;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

import java.io.IOException;
import java.util.Arrays;
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
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarResponse> createCar(
            @RequestPart("metadata") CreateCarDTO carDTO, // application/jason
            @RequestParam("files") MultipartFile[] files) {
        try {

            CarResponse carResponse = carService.handleCreateCar(carDTO, files);
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

    @ApiMessage("Car update successfully!!!")
    @PutMapping(value = "/update/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarResponse> updateCar(
            @RequestPart("metadata") UpdateCarDTO carDTO,
            @PathVariable("carId") Long carId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            CarResponse carResponse = carService.handleUpdateCar(carDTO, files, carId, userId);

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
