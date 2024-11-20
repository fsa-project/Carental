package vn.fsaproject.carental.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.service.RoleService;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

@RestController
@RequestMapping("role")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @ApiMessage("create new role")
    public ResponseEntity<Role> create(@RequestBody Role role) {
        this.roleService.create(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @PutMapping
    @ApiMessage("update")
    public ResponseEntity<Role> update(@RequestBody Role role) {
        this.roleService.update(role);
        return ResponseEntity.status(HttpStatus.OK).body(role);
    }


}
