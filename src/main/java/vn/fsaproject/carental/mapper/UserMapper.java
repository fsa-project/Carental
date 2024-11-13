package vn.fsaproject.carental.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.fsaproject.carental.dto.request.RegisterDTO;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@org.mapstruct.Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role",ignore = true)
    User toUser(RegisterDTO request);
    @Mapping(target = "role",ignore = true)
    UserResponse toUserResponse(User user);
    List<UserResponse> toUserResponseList(List<User> users);
    void updateUser(@MappingTarget User user, UpdateProfileDTO request);

}
