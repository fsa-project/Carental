package vn.fsaproject.carental.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CreateCarDTO {
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
    private String additionalFunctions;
    private String description;
    private List<String> images;
}
