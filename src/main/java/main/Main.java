package main;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Main {

    @Value("${cloud.key}")
    String cloudKey;

    @Value("${cloud.secret}")
    String cloudSecret;

    @Value("${cloud.name}")
    String cloudName;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Cloudinary cloudinaryConfig() {
        Cloudinary cloudinary;
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", cloudKey);
        config.put("api_secret", cloudSecret);
        cloudinary = new Cloudinary(config);
        return cloudinary;
    }

}
