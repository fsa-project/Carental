package vn.fsaproject.carental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.request.RoleDTO;
import vn.fsaproject.carental.dto.response.ApiResponse;
import vn.fsaproject.carental.dto.response.RoleResponse;
import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.service.RoleService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    RoleService roleService;
    @PostMapping("/create")
    ApiResponse<RoleResponse> createRole(@RequestBody RoleDTO request){
        ApiResponse<RoleResponse> response = new ApiResponse<>();
        RoleResponse roleResponse = roleService.createRole(request);
        response.setResults(roleResponse);
        return response;
    }
    @GetMapping("/roles")
    ApiResponse<Set<RoleResponse>> showRoles(){
        ApiResponse<Set<RoleResponse>> response = new ApiResponse<>();
        response.setResults(roleService.getAllRole());
        return response;
    }
    @DeleteMapping("/{user_id}")
    public ApiResponse deleteRole(@PathVariable("{user_id}")Long id){
        ApiResponse response = new ApiResponse();
        response.setCode(1050);
        response.setMessage("Delete role successfully");
        return response;
    }
}
