package main.controller;

import main.api.response.AuthCheckResponse;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final UserRepository userRepository;
    private final AuthCheckService authCheckService;

    public ApiAuthController(UserRepository userRepository, AuthCheckService authCheckService) {
        this.userRepository = userRepository;
        this.authCheckService = authCheckService;
    }

    @GetMapping("/check")
    public ResponseEntity<AuthCheckResponse> check(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(new AuthCheckResponse());
        }
        AuthCheckResponse authCheckResponse = authCheckService.getAuthCheckResponse(principal.getName());
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }
}
