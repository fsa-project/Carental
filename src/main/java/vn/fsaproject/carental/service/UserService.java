package vn.fsaproject.carental.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.dto.request.RegisterDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.RoleResponse;
import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.RoleMapper;
import vn.fsaproject.carental.mapper.UserMapper;
import vn.fsaproject.carental.repository.RoleRepository;
import vn.fsaproject.carental.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       RoleService roleService,
                       RoleRepository roleRepository,
                       RoleMapper roleMapper,
                       PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User handleCreateUser(User user) {
        user.setRole(this.roleService.findByName(user.getRole().getName()));
        System.out.println(user.getRole().getName() + user.getRole().getDescription());
        return userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        userRepository.deleteById(id);
    }

    public UserResponse handleUserById(long id) {
        User user = userRepository.findById(id).orElse(null);
        UserResponse userResponse = userMapper.toUserResponse(user);
        if (user != null) {
            RoleResponse roleResponse = roleMapper.toRoleResponse(user.getRole());
            userResponse.setRole(roleResponse);
        }else {
            userResponse.setRole(null);
        }
        return userResponse;
    }

    public List<User> handleAllUser() {
        return userRepository.findAll();
    }

    public UserResponse handleUpdateUser(UpdateProfileDTO request, Long id) {
        User currentUser = userRepository.findById(id).orElse(null);
        userMapper.updateUser(currentUser, request);
        UserResponse response = userMapper.toUserResponse(currentUser);
        if (currentUser != null) {
            userRepository.save(currentUser);
        }

        return response;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public boolean isEmailExist(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

}
