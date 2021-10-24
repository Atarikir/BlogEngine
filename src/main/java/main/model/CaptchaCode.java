package main.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "captcha_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaCode {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @Column(name = "time")
    @NotNull
    private LocalDateTime time;

    @Column(name = "code")
    @NotNull
    private String code;

    @Column(name = "secret_code")
    @NotNull
    private String secretCode;
}
