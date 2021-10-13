package main.service;

import main.api.response.AuthCheckResponse;
import main.api.response.CaptchaResponse;


public interface AuthCheckService {

    AuthCheckResponse getAuthCheckResponse(String email);

    CaptchaResponse getCaptcha();
}
