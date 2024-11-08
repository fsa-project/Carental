package vn.fsaproject.carental.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;
    private String content;
    private LocalDateTime date;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
