package vn.fsaproject.carental.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.dto.request.RegisterDTO;
import vn.fsaproject.carental.dto.request.UpdateCarDTO;
import vn.fsaproject.carental.dto.request.UpdateProfileDTO;
import vn.fsaproject.carental.dto.response.*;
import vn.fsaproject.carental.entities.Car;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.mapper.RoleMapper;
import vn.fsaproject.carental.mapper.UserMapper;
import vn.fsaproject.carental.repository.RoleRepository;
import vn.fsaproject.carental.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper, RoleService roleService, RoleRepository roleRepository, RoleMapper roleMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    public User handleCreateUser(User user) {
        user.setRole(this.roleService.findByName(user.getRole().getName()));
        System.out.println(user.getRole().getName() + user.getRole().getDescription());
        return userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        userRepository.deleteById(id);
    }
    private TransactionResponse createTransactionResponse(Transaction transaction) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setDescription(transaction.getDescription());
        transactionResponse.setTransactionDate(transaction.getTransactionDate());
        transactionResponse.setTransactionType(transaction.getTransactionType());
        transactionResponse.setBookingId(transaction.getBooking().getId());
        transactionResponse.setCarName(transaction.getBooking().getCar().getName());
        return transactionResponse;
    }
    private DataPaginationResponse createPaginatedResponse(Pageable pageable, List<Transaction> transactions) {
        List<TransactionResponse> responses = transactions.stream()
                .map(this::createTransactionResponse)
                .toList();

        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setSize(pageable.getPageSize());
        meta.setPages((int) Math.ceil((double) transactions.size() / pageable.getPageSize()));
        meta.setTotal(transactions.size());

        DataPaginationResponse response = new DataPaginationResponse();
        response.setMeta(meta);
        response.setResult(responses);
        return response;
    }
    public DataPaginationResponse getUserTransactions(long userId,Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Transaction> transactions = new ArrayList<>(user.getSenders());
        transactions.stream().forEach(transaction -> {transaction.setAmount(-transaction.getAmount());});
        transactions.addAll(user.getRecipients());
        transactions.sort(Comparator.comparingLong(Transaction::getId));


        return createPaginatedResponse(pageable, transactions);
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
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        double wallet = currentUser.getWallet();
        userMapper.updateUser(currentUser, request);
        currentUser.setWallet(wallet);

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
