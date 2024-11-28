package vn.fsaproject.carental.service;

import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.Permission;
import vn.fsaproject.carental.repository.PermissionRepository;

import java.util.List;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean isPermissionExist(Permission permission) {
        return permissionRepository.existsByModuleAndApiPathAndMethod(
                permission.getMethod(),
                permission.getApiPath(),
                permission.getMethod()
        );
    }

    public Permission create(Permission permission) {
        return permissionRepository.save(permission);
    }

    public Permission getPermissionById(long id) {
        return permissionRepository.findById(id).orElse(null);
    }

    public List<Permission> findByIdIn(List<Long> reqPermissions) {
        return permissionRepository.findAllById(reqPermissions);
    }
}
