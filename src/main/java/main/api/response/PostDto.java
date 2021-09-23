package main.api.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostDto {

    private int id;
    private long timestamp;
    private UserDto userDto;
    private String title;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
}