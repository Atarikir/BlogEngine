package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String email;
    private String name;
    private String password;
    private String captcha;
    private String title;
    private String text;
    private String image;
    private String photo;
    private String code;
}
