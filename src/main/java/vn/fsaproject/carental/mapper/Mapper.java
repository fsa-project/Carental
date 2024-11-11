package vn.fsaproject.carental.mapper;

import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.entities.User;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@org.mapstruct.Mapper(componentModel = "spring")
public interface Mapper {
    UserResponse toUserResponse(User user);
}
