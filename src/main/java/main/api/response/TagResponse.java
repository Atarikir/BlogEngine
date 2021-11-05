package main.api.response;

import lombok.Data;

import java.util.List;

@Data
public class TagResponse {
    private List<TagDto> tags;
}
