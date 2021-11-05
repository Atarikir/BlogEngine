package main.api.response;

import lombok.Data;

import java.util.List;

@Data
public class PostResponse {
    private int count;
    private List<PostDto> posts;
}
