package main.controller;

import main.api.request.AuthRegRequest;
import main.api.request.LoginRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.AuthRegisterResponse;
import main.api.response.CaptchaResponse;
import main.model.User;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import main.service.CaptchaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthCheckService authCheckService;
    private final CaptchaService captchaService;
    private final UserRepository userRepository;

    public ApiAuthController(AuthCheckService authCheckService, CaptchaService captchaService, UserRepository userRepository) {
        this.authCheckService = authCheckService;
        this.captchaService = captchaService;
        this.userRepository = userRepository;
    }

    @GetMapping("/check")
    public ResponseEntity<AuthCheckResponse> check(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(new AuthCheckResponse());
        }
        AuthCheckResponse authCheckResponse = authCheckService.getAuthCheck(principal.getName());
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> captcha() {
        CaptchaResponse captchaResponse = captchaService.getCaptcha();
        return new ResponseEntity<>(captchaResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> registration(@RequestBody AuthRegRequest authRegRequest) {
        AuthRegisterResponse authRegisterResponse = authCheckService.createUser(authRegRequest);
        return new ResponseEntity<>(authRegisterResponse, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthCheckResponse> login(@RequestBody LoginRequest loginRequest) {
        User currentUser = userRepository.findByEmail(loginRequest.getEmail());

        if (currentUser == null || !authCheckService.getEncoder().matches(loginRequest.getPassword(),
                currentUser.getPassword())) {
            return ResponseEntity.ok(new AuthCheckResponse());
        }
        AuthCheckResponse authCheckResponse = authCheckService.getLoginUser(loginRequest);
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AuthCheckResponse> logout(HttpServletRequest request) {
        AuthCheckResponse authCheckResponse = authCheckService.getLogoutUser(request);
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }
}
