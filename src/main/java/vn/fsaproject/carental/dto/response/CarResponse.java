package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CarResponse {
    private String name;
    private double basePrice;
    private String address;
    private List<String> images;
}
