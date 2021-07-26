package main.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "captcha_codes")
@Data
public class CaptchaCode {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @Column(name = "time")
    @NotNull
    private Date time;

    @Column(name = "code")
    @NotNull
    private String code;

    @Column(name = "secret_code")
    @NotNull
    private String secretCode;
}
