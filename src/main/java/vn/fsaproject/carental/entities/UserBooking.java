package vn.fsaproject.carental.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users_booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String drivingLicense;
    private String address;
    private String email;
    private String nationalId;

    @OneToMany(mappedBy = "renter")
    @JsonIgnore
    List<Booking> renters;

    @OneToMany(mappedBy = "driver")
    @JsonIgnore
    List<Booking> drivers;

}
