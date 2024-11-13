package vn.fsaproject.carental.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import vn.fsaproject.carental.dto.response.*;
import vn.fsaproject.carental.dto.request.*;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/User")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody RegisterDTO request){
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setMessage("User create successfully!!");
        response.setResults(userService.register(request));
        return response;
    }
    @PostMapping("/update/{user_id}")
    User updateUser(
            @RequestBody UpdateProfileDTO request,
            @PathVariable("user_id") Long id
    ){
        return userService.updateUser(request,id);
    }
    @GetMapping("/all")
    ApiResponse<List<UserResponse>> getUsers(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username: {}",authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResults(userService.getUsers());
        return response;
    }
    @GetMapping("/{user_id}")
    User getUser(@PathVariable("user_id") Long id){
        return userService.getUser(id);
    }
    @DeleteMapping("del/{user_id}")
    void delUser(@PathVariable("user_id")Long id){
        userService.deleteUser(id);
    }
    @DeleteMapping("/del_all")
    void delAllUser(){
        userService.deleteAllUser();
    }
}
