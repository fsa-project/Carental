package vn.fsaproject.carental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.dto.request.RoleDTO;
import vn.fsaproject.carental.dto.response.RoleResponse;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.mapper.RoleMapper;
import vn.fsaproject.carental.repository.RoleDAO;

import javax.management.relation.RoleNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class RoleService {
    @Autowired
    RoleDAO roleDAO;
    @Autowired
    RoleMapper mapper;
    public RoleResponse createRole(RoleDTO request){
        Role role = mapper.toRole(request);
        if (role == null) {
            log.error("Mapper returned null for role");
        } else {
            log.info("Mapped Role: " + role);
        }

        roleDAO.save(role);
        RoleResponse roleResponse = mapper.toRoleResponse(role);
        if (roleResponse == null) {
            log.error("Mapper returned null for RoleResponse");
        }
        return roleResponse;
    }
    public Set<RoleResponse> getAllRole(){
        return mapper.toRoleResponseSet(new HashSet<>(roleDAO.findAll()));
    }
    public void deleteRole(Long id){
        roleDAO.deleteById(id);
    }

    public Role getRole(String name) {
        return roleDAO.findByName(name);
    }

}
