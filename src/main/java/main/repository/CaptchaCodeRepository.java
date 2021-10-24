package main.repository;

import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CaptchaCodeRepository extends JpaRepository<CaptchaCode, Integer> {

    @Modifying
    @Query("delete from CaptchaCode c where c.time < ?1")
    int deleteCaptcha(LocalDateTime time);

    CaptchaCode findByCode(String captcha);
}
