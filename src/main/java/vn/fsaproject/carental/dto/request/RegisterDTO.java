package vn.fsaproject.carental.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private String name;
    private String password;
    private LocalDate dateOfBirth;
    private String nationalIdNo;
    private String phoneNo;
    private String email;
    private String address;
    private String drivingLicense;
    private Double wallet;
}
