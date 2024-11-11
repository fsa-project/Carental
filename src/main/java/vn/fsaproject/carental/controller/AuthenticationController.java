package vn.fsaproject.carental.controller;

import vn.fsaproject.carental.dto.request.AuthenticationDTO;
import vn.fsaproject.carental.dto.request.IntrospectDTO;
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

@RestController
public class AuthenticationController {
    @Autowired
    AuthenticationService service;
    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationDTO request){
        var response = service.login(request);
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>(1000,null,response);
        return apiResponse;
    }
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectDTO request)
            throws JOSEException, ParseException {
        var result = service.introspect(request);

//        AuthenticationResponse authenticationResponse = new AuthenticationResponse(result,message);
//        ApiResponse<AuthenticationResponse> response = new ApiResponse<>(1000,null,authenticationResponse);
//        return response;
        return ApiResponse.<IntrospectResponse>builder()
                .code(1000)
                .results(result)
                .build();
    }
}
