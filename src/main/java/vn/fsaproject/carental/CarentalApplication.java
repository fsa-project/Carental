package vn.fsaproject.carental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import vn.fsaproject.carental.config.SecurityConfig;

@SpringBootApplication
public class CarentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarentalApplication.class, args);
    }

}
