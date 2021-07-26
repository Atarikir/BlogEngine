package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class AuthCheckResponse {

    private boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserFullDto userFullDto;
}
