package vn.fsaproject.carental.utils;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.fsaproject.carental.dto.response.RestResponse;
import vn.fsaproject.carental.utils.annotation.ApiMessage;

@RestControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        RestResponse<Object> restResponse = new RestResponse<Object>();
        restResponse.setStatusCode(status);

        if (body instanceof String) {
            return body;
        }

        if (status >= 400) {
            // case error
            return body;
        } else {
            //case success
            restResponse.setData(body);
            ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);

            restResponse.setMessage(message != null ? message.value() : "Call Api Success");
        }

        return restResponse;
    }
}
