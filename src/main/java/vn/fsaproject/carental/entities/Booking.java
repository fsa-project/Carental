package vn.fsaproject.carental.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "booking")
    private Feedback feedback;

    @OneToMany(mappedBy = "booking")
    private List<Transaction> transactions;

    @ManyToOne
    @JoinColumn(name = "renter_id")
    private UserBooking renter;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private UserBooking driver;

    private Date startDateTime;
    private Date endDateTime;
    private String driversInformation;
    private String paymentMethod;
    private String bookingStatus;
}
