package main.api.request;

import lombok.Data;

@Data
public class ProfileRequest {
    private String name;
    private String email;
    private String password;
    int removePhoto;
}
