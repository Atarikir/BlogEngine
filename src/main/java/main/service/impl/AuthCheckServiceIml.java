package main.service.impl;

import main.api.response.AuthCheckResponse;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import org.springframework.stereotype.Service;

@Service
public class AuthCheckServiceIml implements AuthCheckService {

    private UserRepository userRepository;

    public AuthCheckServiceIml(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthCheckResponse getAuthCheckUser() {
        AuthCheckResponse authCheckResponse = new AuthCheckResponse();
        authCheckResponse.setResult(false);
        return authCheckResponse;
    }
}
