package vn.fsaproject.carental.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String description;
    private String additionalFunctions;
    private String termsOfUse;
    private String carStatus;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarImage> images;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany
    @JoinColumn(name = "car_id")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarDocument> documents;


    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", numberOfSeats=" + numberOfSeats +
                ", productionYears=" + productionYears +
                ", transmissionType='" + transmissionType + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", mileage=" + mileage +
                ", fuelConsumption=" + fuelConsumption +
                ", basePrice=" + basePrice +
                ", deposit=" + deposit +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", additionalFunctions='" + additionalFunctions + '\'' +
                ", termsOfUse='" + termsOfUse + '\'' +
                ", carStatus='" + carStatus + '\'' +
                ", images=" + images +
                ", user=" + user +
                ", bookings=" + bookings +
                '}';
    }
}
