package vn.fsaproject.carental.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String password;
    @Getter
    @Column(unique = true)
    private String name;
    private LocalDate dateOfBirth;
    private String nationalIdNo;
    private String phoneNo;
    private String email;
    private String address;
    private String drivingLicense;
    private Double wallet;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;


    @ManyToOne
    @JoinColumn(name = "role_id") // Cột role_id là khóa ngoại trỏ tới bảng Role
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Car> cars; // Dành cho vai trò Car Owner

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings; // Dành cho vai trò Customer

}
