package vn.fsaproject.carental.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.fsaproject.carental.dto.request.RoleDTO;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.service.RoleService;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

@RestController
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/role/create")
    @ApiMessage("create role")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        this.roleService.handleCreate(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
}
