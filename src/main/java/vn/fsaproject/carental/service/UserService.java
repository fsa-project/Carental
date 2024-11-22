package vn.fsaproject.carental.service;

import org.springframework.stereotype.Service;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    public User handleCreateUser(User user) {
        user.setRole(this.roleService.findByName(user.getRole().getName()));
        System.out.println(user.getRole().getName() + user.getRole().getDescription());
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

    public User handleUpdateUser(User reqUser) {
        User currentUser = this.handleUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setId(reqUser.getId());
        }
        return currentUser;
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
