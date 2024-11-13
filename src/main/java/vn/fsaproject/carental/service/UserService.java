package vn.fsaproject.carental.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vn.fsaproject.carental.dto.request.RegisterDTO;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.exception.AppException;
import vn.fsaproject.carental.exception.ErrorCode;
import vn.fsaproject.carental.mapper.RoleMapper;
import vn.fsaproject.carental.mapper.UserMapper;
import vn.fsaproject.carental.repository.RoleDAO;
import vn.fsaproject.carental.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Service
public class UserService {
    UserDAO userDAO;
    RoleDAO roleDAO;
    UserMapper userMapper;
    RoleMapper roleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public UserResponse register(RegisterDTO request){
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(roleDAO.findByName(request.getRole()));
        userDAO.save(user);
        UserResponse response = userMapper.toUserResponse(user);
        response.setRole(roleMapper.toRoleResponse(roleDAO.findByName(request.getRole())));
        return response;
    }
    public User updateUser(UpdateProfileDTO request, Long id){
        User user = userDAO.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user,request);
        return userDAO.save(user);
    }
    public List<UserResponse> getUsers (){
        return userMapper.toUserResponseList(userDAO.findAll());
    }
    public User getUser(Long id){
        return userDAO.findById(id).orElseThrow(() -> new RuntimeException("user Not Found"));
    }
    public void deleteUser(Long id){
        userDAO.deleteById(id);
    }
    public void deleteAllUser(){
        userDAO.deleteAll();
    }



}
