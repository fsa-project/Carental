package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CarResponse {
    private  Long id;
    private String name;
    private double basePrice;
    private String address;
    private String carStatus;
    private List<String> images;
    private List<String> documents;
}
