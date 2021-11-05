package main.service.impl;

import main.api.request.AuthRegRequest;
import main.api.request.LoginRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.AuthRegisterResponse;
import main.api.response.ErrorResponse;
import main.api.response.UserDto;
import main.model.CaptchaCode;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.CaptchaCodeRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service
public class AuthCheckServiceIml implements AuthCheckService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final AuthenticationManager authenticationManager;

    public AuthCheckServiceIml(UserRepository userRepository, PostRepository postRepository, CaptchaCodeRepository captchaCodeRepository, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.captchaCodeRepository = captchaCodeRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthCheckResponse getAuthCheck(String email) {
        AuthCheckResponse authCheckResponse = new AuthCheckResponse();
        authCheckResponse.setResult(true);
        authCheckResponse.setUser(getUserDto(email));

        return authCheckResponse;
    }

    @Override
    public AuthCheckResponse getLoginUser(LoginRequest loginRequest) {
        Authentication auth = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User)
                auth.getPrincipal();

        AuthCheckResponse authCheckResponse = new AuthCheckResponse();
        authCheckResponse.setResult(true);
        authCheckResponse.setUser(getUserDto(user.getUsername()));

        return authCheckResponse;
    }

    @Override
    public AuthCheckResponse getLogoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        AuthCheckResponse authCheckResponse = new AuthCheckResponse();
        authCheckResponse.setResult(true);
        return authCheckResponse;
    }

    @Override
    public String getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        return user.getUsername();
    }

    @Override
    public BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Override
    public AuthRegisterResponse createUser(AuthRegRequest authRegRequest) {
        int minLengthPassword = 6;
        String errorEmail = "Этот e-mail уже зарегистрирован";
        String errorName = "Имя указано неверно";
        String errorPassword = "Пароль короче 6-ти символов";
        String errorCaptcha = "Код с картинки введён неверно";
        String name = authRegRequest.getName();
        String password = authRegRequest.getPassword();
        CaptchaCode captchaCodeDB = captchaCodeRepository.findByCode(authRegRequest.getCaptcha());
        User user = userRepository.findByEmail(authRegRequest.getEmail());

        if (user != null) {
            return AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .email(errorEmail)
                            .build())
                    .build();
        }

        if (!name.matches("^[а-яА-ЯёЁa-zA-Z0-9]{2,20}$")) {
            return AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .name(errorName)
                            .build())
                    .build();
        }

        if (password.length() < minLengthPassword) {
            return AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .password(errorPassword)
                            .build())
                    .build();
        }

        if (captchaCodeDB == null || !captchaCodeDB.getSecretCode().equals(authRegRequest.getCaptchaSecret())) {
            return AuthRegisterResponse.builder()
                    .result(false)
                    .errors(ErrorResponse.builder()
                            .captcha(errorCaptcha)
                            .build())
                    .build();
        }

        userRepository.save(User.builder()
                .isModerator((byte) 0)
                .regTime(LocalDateTime.now())
                .name(name)
                .email(authRegRequest.getEmail())
                .password(encodeBCrypt(authRegRequest.getPassword()))
                .build());

        return AuthRegisterResponse.builder()
                .result(true)
                .build();
    }

    public UserDto getUserDto(String email) {
        User currentUser = userRepository.findByEmail(email);

        return UserDto.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .photo(currentUser.getPhoto())
                .email(currentUser.getEmail())
                .moderation(currentUser.isModerator())
                .moderationCount(currentUser.isModerator() ? getModerationCount() : 0)
                .settings(currentUser.isModerator())
                .build();
    }

    public String encodeBCrypt(String password) {
        return getEncoder().encode(password);
    }

    public int getModerationCount() {
        byte isActive = 1;
        ModerationStatus moderationStatus = ModerationStatus.NEW;
        return postRepository.countByIsActiveAndModerationStatus(isActive, moderationStatus);
    }
}
