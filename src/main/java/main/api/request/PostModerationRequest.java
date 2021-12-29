package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PostModerationRequest {

    @JsonProperty("post_id")
    private int postId;

    private String decision;
}
