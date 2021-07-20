package main.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.dto.TagDto;

import java.util.List;

@Data
@NoArgsConstructor
public class TagResponse {

    private List<TagDto> tags;
}
