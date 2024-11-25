package vn.fsaproject.carental.controller;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.exception.IdInvalidException;
import vn.fsaproject.carental.service.UserService;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @ApiMessage("Create a new user")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExist(user.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.handleDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) throws IdInvalidException {
        if (id > 1500) {
            throw new IdInvalidException("Id invalid");
        }
        return ResponseEntity.ok(this.userService.handleUserById(id));
    }

    @GetMapping
    @ApiMessage("Fetch all user")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleAllUser());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateProfileDTO request, @PathVariable long id) throws IdInvalidException {
        return ResponseEntity.ok(this.userService.handleUpdateUser(request,id));
    }
}
