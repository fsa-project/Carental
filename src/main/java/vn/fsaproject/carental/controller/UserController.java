package vn.fsaproject.carental.controller;

import vn.fsaproject.carental.dto.response.*;
import vn.fsaproject.carental.dto.request.*;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    List<User> getUsers(){
        return userService.getUsers();
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
