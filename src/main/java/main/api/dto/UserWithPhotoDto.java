package main.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserWithPhotoDto {

    private int id;
    private String name;
    private String photo;
}
