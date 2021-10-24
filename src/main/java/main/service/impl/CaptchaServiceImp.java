package main.service.impl;

import com.github.cage.Cage;
import com.github.cage.GCage;
import main.api.response.CaptchaResponse;
import main.model.CaptchaCode;
import main.repository.CaptchaCodeRepository;
import main.service.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class CaptchaServiceImp implements CaptchaService {

    @Value("${captcha.deleteLimitTime}")
    private int deleteLimitTime;

    private final CaptchaCodeRepository captchaCodeRepository;

    public CaptchaServiceImp(CaptchaCodeRepository captchaCodeRepository) {
        this.captchaCodeRepository = captchaCodeRepository;
    }

    @Override
    public CaptchaResponse getCaptcha() {
        String heading = "data:image/png;base64, ";
        Cage cage = new GCage();
        String token = cage.getTokenGenerator().next();
        String code = generateCode(cage, token);
        String secretCode = UUID.randomUUID().toString();

        captchaCodeRepository.deleteCaptcha(LocalDateTime.now().minusHours(deleteLimitTime));

        captchaCodeRepository.save(CaptchaCode.builder()
                .time(LocalDateTime.now())
                .code(token)
                .secretCode(secretCode)
                .build());

        CaptchaResponse captchaResponse = new CaptchaResponse();
        captchaResponse.setSecret(secretCode);
        captchaResponse.setImage(heading + code);

        return captchaResponse;
    }

    public String generateCode(Cage cage, String token) {
        int resizeWidth = 100;
        int resizeHeight = 35;
        BufferedImage image = cage.drawImage(token);

        BufferedImage resizeImage = new BufferedImage(resizeWidth, resizeHeight, image.getType());

        Graphics2D g2d = resizeImage.createGraphics();
        g2d.drawImage(image, 0, 0, resizeWidth, resizeHeight, null);
        g2d.dispose();
        image = resizeImage;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] imageBytes = bos.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
