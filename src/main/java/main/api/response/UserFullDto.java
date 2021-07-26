package main.api.response;

import lombok.Data;

@Data
public class UserFullDto extends UserWithPhotoDto{

    private String email;
    private boolean moderation;
    private int moderationCount;
    private boolean settings;
}
