package vn.fsaproject.carental.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;
import vn.fsaproject.carental.dto.request.CreateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.CarResponse;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.User;

import java.util.List;

@Component
@org.mapstruct.Mapper(componentModel = "spring")
public interface CarMapper {
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "documents", ignore = true)
    Car toCar(CreateCarDTO carDTO);
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "documents", ignore = true)
    CarResponse toCarResponse(Car car);
    List<CarResponse> toCarResponses(List<Car> cars);
    @Mapping(target = "images",ignore = true)
    @Mapping(target = "documents", ignore = true)
    void updateCar(@MappingTarget Car car, UpdateCarDTO request);
}
