package vn.fsaproject.carental.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.fsaproject.carental.dto.request.CreateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.response.CarResponse;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.service.CarService;
import vn.fsaproject.carental.utils.SecurityUtil;

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
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarResponse> createCar(
            @RequestPart("metadata") CreateCarDTO carDTO, // application/jason
            @RequestParam("files") MultipartFile[] files
    ){
        try {

            CarResponse carResponse = carService.handleCreateCar(carDTO, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(carResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/user-cars")
    public ResponseEntity<List<CarResponse>> getUserCars(){
        try{
            Long userID = securityUtil.getCurrentUserId();
            List<CarResponse> carResponses = carService.handleGetCars(userID);
            if(carResponses.isEmpty()){
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(carResponses);
        }catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<CarResponse> updateCar(@RequestBody UpdateCarDTO carDTO, @PathVariable("id") Long id){
        return ResponseEntity.ok(this.carService.handleUpdateCar(carDTO, id));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable("id") Long id){
        this.carService.handleDeleteCar(id);
        return ResponseEntity.noContent().build();
    }

}
