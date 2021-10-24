package main.service;

import main.api.request.AuthRegRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.AuthRegisterResponse;
import org.springframework.http.ResponseEntity;


public interface AuthCheckService {

    AuthCheckResponse getAuthCheckResponse(String email);

    ResponseEntity<AuthRegisterResponse> createUser(AuthRegRequest authRegRequest);

}
