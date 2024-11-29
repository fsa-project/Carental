package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CarDetailResponse {
    private Long id;
    private String name;
    private String licensePlate;
    private String brand;
    private String model;
    private String color;
    private int numberOfSeats;
    private int productionYears;
    private String transmissionType;
    private String fuelType;
    private double mileage;
    private double fuelConsumption;
    private double basePrice;
    private double deposit;
    private String address;
    private String carStatus;
    private String description;
    private String additionalFunctions;
    private String termsOfUse;
    private List<String> images;
    private List<String> documents;
}
