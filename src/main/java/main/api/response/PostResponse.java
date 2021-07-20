package main.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.dto.PostDto;

import java.util.List;

@Data
@NoArgsConstructor
public class PostResponse {

    private int count;
    private List<PostDto> posts;
}
