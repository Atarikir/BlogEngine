package main.service.impl;

import main.api.request.AuthRegRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.AuthRegisterResponse;
import main.api.response.ErrorResponse;
import main.api.response.UserDto;
import main.model.CaptchaCode;
import main.model.User;
import main.repository.CaptchaCodeRepository;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthCheckServiceIml implements AuthCheckService {

    private final UserRepository userRepository;
    private final CaptchaCodeRepository captchaCodeRepository;

    public AuthCheckServiceIml(UserRepository userRepository, CaptchaCodeRepository captchaCodeRepository) {
        this.userRepository = userRepository;
        this.captchaCodeRepository = captchaCodeRepository;
    }

    @Override
    public AuthCheckResponse getAuthCheckResponse(String email) {
        User currentUser = userRepository.findByEmail(email);
        //.orElseThrow(() -> new UsernameNotFoundException(email));

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

    public ResponseEntity<AuthRegisterResponse> createUser(AuthRegRequest authRegRequest) {
        int minLengthPassword = 6;
        String errorEmail = "Этот e-mail уже зарегистрирован";
        String errorName = "Имя указано неверно";
        String errorPassword = "Пароль короче 6-ти символов";
        String errorCaptcha = "Код с картинки введён неверно";
        String name = authRegRequest.getName();
        String password = authRegRequest.getPassword();
        String captchaCode = authRegRequest.getCaptcha();
        CaptchaCode captchaCodeDB = captchaCodeRepository.findByCode(authRegRequest.getCaptcha());
        User user = userRepository.findByEmail(authRegRequest.getEmail());

        if (user != null) {
            return ResponseEntity.ok(AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .email(errorEmail)
                            .build())
                    .build());
        }

        if (!name.matches("^[а-яА-ЯёЁa-zA-Z0-9]{2,20}$")) {
            return ResponseEntity.ok(AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .name(errorName)
                            .build())
                    .build());
        }

        if (password.length() < minLengthPassword) {
            return ResponseEntity.ok(AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .password(errorPassword)
                            .build())
                    .build());
        }

        if (captchaCodeDB == null || !captchaCodeDB.getSecretCode().equals(authRegRequest.getCaptchaSecret())) {
            return ResponseEntity.ok(AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .captcha(errorCaptcha)
                            .build())
                    .build());
        }

        userRepository.save(User.builder()
                .isModerator((byte) 0)
                .regTime(LocalDateTime.now())
                .name(name)
                .email(authRegRequest.getEmail())
                .password(encodeBCrypt(authRegRequest.getPassword()))
                .build());

        return ResponseEntity.ok(AuthRegisterResponse.builder()
                .result(true)
                .build());
    }

    public String encodeBCrypt(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
}
