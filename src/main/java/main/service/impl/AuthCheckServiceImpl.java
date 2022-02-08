package main.service.impl;

import main.api.request.AuthRegRequest;
import main.api.request.LoginRequest;
import main.api.response.AuthCheckResponse;
import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.api.response.UserDto;
import main.exceptions.NoFoundException;
import main.model.CaptchaCode;
import main.model.GlobalSetting;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.CaptchaCodeRepository;
import main.repository.GlobalSettingRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import main.service.UtilityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;

@Service
public class AuthCheckServiceImpl implements AuthCheckService {

    @Value("${settings.value.false}")
    private String settingValueFalse;

    @Value("${settings.code.multiuserMode}")
    private String multiuserMode;

    @Value("${user.namePattern}")
    private String namePattern;

    @Value("${user.minLengthPassword}")
    private int minLengthPassword;

    @Value("${user.errorEmail}")
    private String errorEmail;

    @Value("${user.errorName}")
    private String errorName;

    @Value("${user.errorPassword}")
    private String errorPassword;

    @Value("${user.errorCaptcha}")
    private String errorCaptcha;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final AuthenticationManager authenticationManager;
    private final UtilityService utilityService;
    private final GlobalSettingRepository globalSettingRepository;

    public AuthCheckServiceImpl(UserRepository userRepository,
                                PostRepository postRepository, CaptchaCodeRepository captchaCodeRepository,
                                AuthenticationManager authenticationManager,
                                UtilityService utilityService, GlobalSettingRepository globalSettingRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.captchaCodeRepository = captchaCodeRepository;
        this.authenticationManager = authenticationManager;
        this.utilityService = utilityService;
        this.globalSettingRepository = globalSettingRepository;
    }

    @Override
    public AuthCheckResponse getAuthCheck(Principal principal) {
        if (principal == null) {
            return new AuthCheckResponse();
        }

        AuthCheckResponse authCheckResponse = new AuthCheckResponse();
        authCheckResponse.setResult(true);
        authCheckResponse.setUser(getUserDto(principal.getName()));

        return authCheckResponse;
    }

    @Override
    public AuthCheckResponse getLoginUser(LoginRequest loginRequest) {
        User currentUser = userRepository.findByEmail(loginRequest.getEmail());

        if (currentUser == null || !utilityService.getEncoder().matches(loginRequest.getPassword(),
                currentUser.getPassword())) {
            return new AuthCheckResponse();
        }

        Authentication auth = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        AuthCheckResponse authCheckResponse = new AuthCheckResponse();
        authCheckResponse.setResult(true);
        authCheckResponse.setUser(getUserDto(getLoggedInUser(auth)));

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
    public ResultErrorResponse createUser(AuthRegRequest authRegRequest) {
        String name = authRegRequest.getName();
        String password = authRegRequest.getPassword();
        CaptchaCode captchaCodeDB = captchaCodeRepository.findByCode(authRegRequest.getCaptcha());
        User user = userRepository.findByEmail(authRegRequest.getEmail());
        GlobalSetting globalSetting = globalSettingRepository.findByCode(multiuserMode);

        if (globalSetting.getValue().equals(settingValueFalse)) {
            throw new NoFoundException();
        }

        if (user != null) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .email(errorEmail)
                            .build());
        }

        if (!name.matches(namePattern)) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .name(errorName)
                            .build());
        }

        if (password.length() < minLengthPassword) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .password(errorPassword)
                            .build());
        }

        if (captchaCodeDB == null || !captchaCodeDB.getSecretCode().equals(authRegRequest.getCaptchaSecret())) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .captcha(errorCaptcha)
                            .build()
            );
        }

        userRepository.save(
                User.builder()
                        .isModerator((byte) 0)
                        .regTime(utilityService.getTimeNow())
                        .name(name)
                        .email(authRegRequest.getEmail())
                        .password(utilityService.encodeBCrypt(authRegRequest.getPassword()))
                        .build()
        );

        return utilityService.getResultTrue();
    }

    @Override
    public String getLoggedInUser(Authentication auth) {
        org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        return user.getUsername();
    }

    @Override
    public boolean isUserAuthorize() {
        return userRepository.findByEmail(getLoggedUserName()) != null;
    }

    @Override
    public main.model.User getAuthorizedUser() {
        return userRepository.findByEmail(getLoggedUserName());
    }

    private String getLoggedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getLoggedInUser(authentication);
    }


    private UserDto getUserDto(String email) {
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

    private int getModerationCount() {
        byte isActive = 1;
        ModerationStatus moderationStatus = ModerationStatus.NEW;
        return postRepository.countByIsActiveAndModerationStatus(isActive, moderationStatus);
    }
}
