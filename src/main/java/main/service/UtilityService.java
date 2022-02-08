package main.service;

import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

public interface UtilityService {
    LocalDateTime getTimeNow();

    ResultErrorResponse getResultTrue();

    ResultErrorResponse getResultFalse();

    ResultErrorResponse errorsRequest(ErrorResponse errors);

    String encodeBCrypt(String password);

    BCryptPasswordEncoder getEncoder();
}
