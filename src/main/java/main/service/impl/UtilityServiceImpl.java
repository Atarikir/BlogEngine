package main.service.impl;

import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.service.UtilityService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class UtilityServiceImpl implements UtilityService {
    @Override
    public LocalDateTime getTimeNow() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    @Override
    public ResultErrorResponse getResultTrue() {
        return ResultErrorResponse.builder()
                .result(true)
                .build();
    }

    @Override
    public ResultErrorResponse getResultFalse() {
        return ResultErrorResponse.builder()
                .result(false)
                .build();
    }

    @Override
    public ResultErrorResponse errorsRequest(ErrorResponse error) {
        return ResultErrorResponse.builder()
                .result(false)
                .errors(error)
                .build();
    }

    @Override
    public String encodeBCrypt(String password) {
        return getEncoder().encode(password);
    }

    @Override
    public BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
