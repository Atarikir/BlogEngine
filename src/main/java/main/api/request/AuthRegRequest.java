package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthRegRequest {

    @JsonProperty("e_mail")
    private String email;

    private String code;
    private String password;
    private String name;
    private String captcha;

    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
