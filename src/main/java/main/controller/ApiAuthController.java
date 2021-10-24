package main.controller;

import main.api.request.AuthRegRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.AuthRegisterResponse;
import main.api.response.CaptchaResponse;
import main.service.AuthCheckService;
import main.service.CaptchaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthCheckService authCheckService;
    private final CaptchaService captchaService;

    public ApiAuthController(AuthCheckService authCheckService, CaptchaService captchaService) {
        this.authCheckService = authCheckService;
        this.captchaService = captchaService;
    }

    @GetMapping("/check")
    public ResponseEntity<AuthCheckResponse> check(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(new AuthCheckResponse());
        }
        AuthCheckResponse authCheckResponse = authCheckService.getAuthCheckResponse(principal.getName());
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> captcha() {
        CaptchaResponse captchaResponse = captchaService.getCaptcha();
        return new ResponseEntity<>(captchaResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> registration(@RequestBody AuthRegRequest authRegRequest) {
        return authCheckService.createUser(authRegRequest);
    }
}
