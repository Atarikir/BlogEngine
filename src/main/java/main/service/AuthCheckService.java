package main.service;

import main.api.request.AuthRegRequest;
import main.api.request.ProfileRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.ResultErrorResponse;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;


public interface AuthCheckService {

    AuthCheckResponse getAuthCheck(Principal principal);

    ResultErrorResponse createUser(AuthRegRequest authRegRequest);

    AuthCheckResponse getLogoutUser(HttpServletRequest request);

    String getLoggedInUser(Authentication auth);

    boolean isUserAuthorize();

    main.model.User getAuthorizedUser();

    AuthCheckResponse getLoginUser(AuthRegRequest authRegRequest);

    ResultErrorResponse passwordRecovery(ProfileRequest profileRequest);
}
