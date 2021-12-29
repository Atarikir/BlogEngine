package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @JsonProperty("parent_id")
    private Integer parentId;

    @JsonProperty("post_id")
    private int postId;

    private String text;
}
