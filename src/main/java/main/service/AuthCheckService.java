package main.service;

import main.api.request.AuthRegRequest;
import main.api.request.LoginRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.ResultErrorResponse;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;


public interface AuthCheckService {

    AuthCheckResponse getLoginUser(LoginRequest loginRequest);

    AuthCheckResponse getAuthCheck(Principal principal);

    ResultErrorResponse createUser(AuthRegRequest authRegRequest);

    AuthCheckResponse getLogoutUser(HttpServletRequest request);

    String getLoggedInUser(Authentication auth);
}
