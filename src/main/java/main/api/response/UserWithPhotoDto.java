package main.api.response;

import lombok.Data;

@Data
public class UserWithPhotoDto extends UserIdDto{

    private String photo;
}
