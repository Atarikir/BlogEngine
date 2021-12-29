package main.api.request;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {
    private Long timestamp;
    private Byte active;
    private String title;
    private List<String> tags;
    private String text;
}
