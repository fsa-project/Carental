package vn.fsaproject.carental.mapper;

import org.springframework.stereotype.Component;
import vn.fsaproject.carental.dto.request.RoleDTO;
import vn.fsaproject.carental.dto.response.RoleResponse;
import vn.fsaproject.carental.entities.Role;

import java.util.List;
import java.util.Set;

@Component
@org.mapstruct.Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toRole(RoleDTO roleDTO);
    RoleResponse toRoleResponse(Role role);
    Set<RoleResponse> toRoleResponseSet(Set<Role> roles);
}
