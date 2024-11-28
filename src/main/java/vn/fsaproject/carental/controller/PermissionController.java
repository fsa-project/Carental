package vn.fsaproject.carental.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.fsaproject.carental.entities.Permission;
import vn.fsaproject.carental.exception.IdInvalidException;
import vn.fsaproject.carental.service.PermissionService;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/permissions")
public class PermissionController {
    private final PermissionService permissionService;
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/create")
    @ApiMessage("create permission")
    ResponseEntity<Permission> createPermission(@RequestBody Permission permission) throws IdInvalidException {
        if (this.permissionService.isPermissionExist(permission)) {
            throw new IdInvalidException("Permission already exist");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.create(permission));
    }
}
