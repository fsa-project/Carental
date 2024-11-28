package vn.fsaproject.carental.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.entities.Permission;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.service.PermissionService;
import vn.fsaproject.carental.service.RoleService;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/role")
public class RoleController {
    private final RoleService roleService;
    private final PermissionService permissionService;
    public RoleController(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @PostMapping("/create")
    @ApiMessage("create new role")
    public ResponseEntity<Role> create(@RequestBody Role role) {
        this.roleService.create(role);
        if (role.getPermissions() != null) {
            List<Long> reqPermissions = role.getPermissions().stream().map(Permission::getId)
                    .collect(Collectors.toList());
            List<Permission> dbPermissions = this.permissionService.findByIdIn(reqPermissions);
            role.setPermissions(dbPermissions);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @PutMapping
    @ApiMessage("update")
    public ResponseEntity<Role> update(@RequestBody Role role) {


        if (role.getPermissions() != null) {
            List<Long> reqPermissions = role.getPermissions().stream().map(Permission::getId)
                    .collect(Collectors.toList());
            List<Permission> dbPermissions = this.permissionService.findByIdIn(reqPermissions);
            role.setPermissions(dbPermissions);
        }
        this.roleService.update(role);

        return ResponseEntity.status(HttpStatus.OK).body(role);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("delete role")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.roleService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }


}
