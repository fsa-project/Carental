package vn.fsaproject.carental.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.fsaproject.carental.dto.request.AuthenticationDTO;
import vn.fsaproject.carental.dto.request.IntrospectDTO;
import vn.fsaproject.carental.dto.request.LogoutDTO;
import vn.fsaproject.carental.dto.request.RefreshDTO;
import vn.fsaproject.carental.dto.response.ApiResponse;
import vn.fsaproject.carental.dto.response.AuthenticationResponse;
import vn.fsaproject.carental.dto.response.IntrospectResponse;
import vn.fsaproject.carental.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    AuthenticationService service;
    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationDTO request){

        var response = service.login(request);
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>(1000,null,response);
        return apiResponse;
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutDTO request)
            throws ParseException, JOSEException {
        service.logout(request);
        ApiResponse<Void> response = new ApiResponse<>(1000,null,null);
        return response;
    }
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshDTO request)
            throws ParseException, JOSEException {
        var result = service.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().results(result).build();
    }
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectDTO request)
            throws JOSEException, ParseException {
        var result = service.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .code(1000)
                .results(result)
                .build();
    }
}
