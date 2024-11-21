package vn.fsaproject.carental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import vn.fsaproject.carental.entities.Permission;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.exception.IdInvalidException;
import vn.fsaproject.carental.service.UserService;
import vn.fsaproject.carental.utils.SecurityUtil;

import java.util.List;

public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;

    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path: " + path);
        System.out.println(">>> httpMethod: " + httpMethod);
        System.out.println(">>> requestURI: " + requestURI);
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : null;

        if (email != null && !email.isEmpty()) {
            User user = userService.handleGetUserByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean hasPermission = permissions.stream().anyMatch(item -> item.getApiPath().equals(path) && item.getMethod().equals(httpMethod));

                    if (!hasPermission) {
                        throw new IdInvalidException("You are not allowed to access this resource");
                    }
                } else {
                    throw new IdInvalidException("You are not allowed to access this resource");
                }
            }
        }
        return true;
    }
}
