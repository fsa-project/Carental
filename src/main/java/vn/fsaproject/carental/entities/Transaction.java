package vn.fsaproject.carental.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trans_id;
    private int amount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
