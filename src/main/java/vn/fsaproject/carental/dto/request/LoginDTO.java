package vn.fsaproject.carental.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    @NotBlank(message = "email can not be null")
    private String username;
    @NotBlank(message = "password can not be null")
    private String password;
}
