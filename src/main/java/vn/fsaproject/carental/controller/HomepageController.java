package vn.fsaproject.carental.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.fsaproject.carental.dto.response.ApiResponse;

@RestController
@RequestMapping
public class HomepageController {
    @GetMapping("/")
    public ApiResponse<Void> home() {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Welcome to the Home Page");
        apiResponse.setCode(122);
        return apiResponse;
    }
}
