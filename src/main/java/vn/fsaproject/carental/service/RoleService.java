package vn.fsaproject.carental.service;

import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Permission;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.repository.PermissionRepository;
import vn.fsaproject.carental.repository.RoleRepository;

import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public Role findById(long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.orElse(null);
    }
    public Role addPermission(Long roleId, Long permissionId) {
        Role role = findById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(()->new RuntimeException("Permission not found"));
        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    public Role create(Role role) {
        return roleRepository.save(role);
    }

    public Role update(Role role) {
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    public Role findByName(String roleName) {
        return this.roleRepository.findByName(roleName);
    }
}
