package vn.fsaproject.carental.service;

import vn.fsaproject.carental.dto.request.RegisterDTO;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.UserResponse;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.Mapper;
import vn.fsaproject.carental.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private Mapper mapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public UserResponse register(RegisterDTO request){
        User user = new User();
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setNationalIdNo(request.getNationalIdNo());
        user.setPhoneNo(request.getPhoneNo());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setDrivingLicense(request.getDrivingLicense());
        user.setWallet(request.getWallet());
        userDAO.save(user);
        return mapper.toUserResponse(user);
    }
    public User updateUser(UpdateProfileDTO request, Long id){
        User user = userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("user Not Found"));
        user.setName(request.getName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setNationalIdNo(request.getNationalIdNo());
        user.setPhoneNo(request.getPhoneNo());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setDrivingLicense(request.getDrivingLicense());
        user.setWallet(request.getWallet());
        return userDAO.save(user);
    }
    public List<User> getUsers (){
        return userDAO.findAll();
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
