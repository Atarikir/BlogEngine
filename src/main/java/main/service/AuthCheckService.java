package main.service;

import main.api.request.AuthRegRequest;
import main.api.request.LoginRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.AuthRegisterResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpServletRequest;


public interface AuthCheckService {

    AuthCheckResponse getLoginUser(LoginRequest loginRequest);

    AuthCheckResponse getAuthCheck(String email);

    AuthRegisterResponse createUser(AuthRegRequest authRegRequest);

    AuthCheckResponse getLogoutUser(HttpServletRequest request);

    String getLoggedInUser();

    BCryptPasswordEncoder getEncoder();
}
