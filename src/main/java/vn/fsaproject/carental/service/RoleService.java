package vn.fsaproject.carental.service;

import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.repository.RoleRepository;

import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public Role fetchById(long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.orElse(null);
    }
    public Role handleCreate(Role role) {
        return roleRepository.save(role);
    }
}
