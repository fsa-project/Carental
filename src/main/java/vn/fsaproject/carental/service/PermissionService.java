package vn.fsaproject.carental.service;

import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Permission;
import vn.fsaproject.carental.repository.PermissionRepository;

import java.util.Optional;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission fetchById(long id) {
        Optional<Permission> permission = permissionRepository.findById(id);
        return permission.orElse(null);
    }




}
