package vn.fsaproject.carental.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.fsaproject.carental.dto.response.RestResponse;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper mapper;

    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        this.delegate.commence(request, response, authException);
        response.setContentType("application/json; charset=utf-8");

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        String errorMsg = Optional.ofNullable(authException.getCause())
                .map(Throwable::getMessage)
                .orElse(authException.getMessage());

        res.setError(errorMsg);
        res.setMessage("Token is invalid(expire, not correct format)");

        mapper.writeValue(response.getWriter(), res);
    }
}