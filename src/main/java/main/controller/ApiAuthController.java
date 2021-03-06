package main.controller;

import main.api.request.AuthRegRequest;
import main.api.request.ProfileRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.CaptchaResponse;
import main.api.response.ResultErrorResponse;
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

    public ApiAuthController(AuthCheckService authCheckService, CaptchaService captchaService) {
        this.authCheckService = authCheckService;
        this.captchaService = captchaService;
    }

    @GetMapping("/check")
    public ResponseEntity<AuthCheckResponse> check(Principal principal) {
        AuthCheckResponse authCheckResponse = authCheckService.getAuthCheck(principal);
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> captcha() {
        CaptchaResponse captchaResponse = captchaService.getCaptcha();
        return new ResponseEntity<>(captchaResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ResultErrorResponse> registration(@RequestBody AuthRegRequest authRegRequest) {
        ResultErrorResponse resultErrorResponse = authCheckService.createUser(authRegRequest);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthCheckResponse> login(@RequestBody AuthRegRequest authRegRequest) {
        AuthCheckResponse authCheckResponse = authCheckService.getLoginUser(authRegRequest);
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AuthCheckResponse> logout(HttpServletRequest request) {
        AuthCheckResponse authCheckResponse = authCheckService.getLogoutUser(request);
        return new ResponseEntity<>(authCheckResponse, HttpStatus.OK);
    }

    @PostMapping("/restore")
    public ResponseEntity<ResultErrorResponse> passwordRecovery(@RequestBody ProfileRequest profileRequest,
                                                                HttpServletRequest servletRequest) {
        ResultErrorResponse resultErrorResponse = authCheckService.passwordRecovery(profileRequest, servletRequest);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }

    @PostMapping("/password")
    public ResponseEntity<ResultErrorResponse> changePassword(@RequestBody AuthRegRequest authRegRequest) {
        ResultErrorResponse resultErrorResponse = authCheckService.changePassword(authRegRequest);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }
}
