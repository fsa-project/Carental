package vn.fsaproject.carental.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.fsaproject.carental.dto.request.LoginDTO;
import vn.fsaproject.carental.dto.response.ResLoginDTO;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.exception.IdInvalidException;
import vn.fsaproject.carental.service.UserService;
import vn.fsaproject.carental.utils.SecurityUtil;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    @Value("${jesse.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, UserService userService) {
        this.securityUtil = securityUtil;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userService = userService;
    }

    @PostMapping("/login")
    @ApiMessage("login to carental")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // chọc input là username với password vào security
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        // xác thực người dùng(need viết hàm chính xác hơn là ghi đè lại hàm loadUserByUsername) để xác thực với db
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

        //set info người dùng đăng nhập vào context( sau này có the sử dụng)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUser = this.userService.handleGetUserByUsername(loginDTO.getUsername());

        ResLoginDTO.UserLogin userLogin = currentUser == null ? new ResLoginDTO.UserLogin() :
                new ResLoginDTO.UserLogin(currentUser.getId(), currentUser.getEmail(), currentUser.getName());
        res.setUser(userLogin);

        //create a token
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res.getUser());
        res.setAccessToken(access_token);

        //create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);
        this.userService.updateUserToken(refresh_token, loginDTO.getUsername());

        //set cookie
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);
    }

    @GetMapping("/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : null;

        User currentUser = this.userService.handleGetUserByUsername(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();


        if (currentUser != null) {
            userLogin.setId(currentUser.getId());
            userLogin.setEmail(currentUser.getEmail());
            userLogin.setName(currentUser.getName());
            userGetAccount.setUser(userLogin);
        }

        return ResponseEntity.ok().body(userGetAccount);
    }

    @GetMapping("/refresh")
    @ApiMessage("Get user by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue(name = "refresh_token") String refreshToken) {
        //check valid token
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        //check user by email and refresh token
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh Token invalid");
        }

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleGetUserByUsername(email);

        ResLoginDTO.UserLogin userLogin = currentUserDB == null ? new ResLoginDTO.UserLogin() :
                new ResLoginDTO.UserLogin(currentUserDB.getId(), currentUserDB.getEmail(), currentUserDB.getName());
        res.setUser(userLogin);

        //create access token
        String access_token = this.securityUtil.createAccessToken(email, res.getUser());
        res.setAccessToken(access_token);

        //create refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(email, res);
        this.userService.updateUserToken(new_refresh_token, email);

        //set cookie
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);
    }

    @PostMapping("/logout")
    @ApiMessage("Sign out of system")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : null;

        if (email.equals("")) {
            throw new IdInvalidException("Access token invalid");
        }

        //update refresh token bằng null

        this.userService.updateUserToken(null, email);

        // xóa refresh token ở cookie bằng cách set expire time bằng 0
        ResponseCookie deleteCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(null);
    }
}