package vn.fsaproject.carental.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.dto.request.RegisterDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.UserMapper;
import vn.fsaproject.carental.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public User handleCreateUser(User user) {
        return userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        userRepository.deleteById(id);
    }

    public User handleUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> handleAllUser() {
        return userRepository.findAll();
    }

    public UserResponse handleUpdateUser(UpdateProfileDTO request, Long id) {
        User currentUser = this.handleUserById(id);
        userMapper.updateUser(currentUser,request);
        UserResponse response = userMapper.toUserResponse(currentUser);
        userRepository.save(currentUser);

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
}
