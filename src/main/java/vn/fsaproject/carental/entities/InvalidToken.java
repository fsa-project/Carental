package vn.fsaproject.carental.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvalidToken {
    @Id
    private String id;
    private Date expireTime;
}
