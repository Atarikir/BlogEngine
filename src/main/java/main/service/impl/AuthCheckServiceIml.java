package main.service.impl;

import main.api.response.AuthCheckResponse;
import main.api.response.CaptchaResponse;
import main.api.response.UserDto;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthCheckServiceIml implements AuthCheckService {

    private final UserRepository userRepository;

    public AuthCheckServiceIml(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthCheckResponse getAuthCheckResponse(String email) {
        main.model.User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        AuthCheckResponse authCheckResponse = new AuthCheckResponse();
        authCheckResponse.setResult(true);
        authCheckResponse.setUser(UserDto.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .moderation(currentUser.getIsModerator() == 1)
                .build());

        return authCheckResponse;
    }

    @Override
    public CaptchaResponse getCaptcha() {
        return null;
    }
}
