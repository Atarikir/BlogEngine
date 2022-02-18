package main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImageUploadConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/upload/**", "/js/**", "/css/**", "/fonts/**", "/img/**",
                        "/templates/**", "/post/upload/**", "/edit/upload/**", "/avatars/**")
                .addResourceLocations(
                        "file:upload/",
                        "file:avatars/",
                        "classpath:static/js/",
                        "classpath:static/css/",
                        "classpath:static/fonts/",
                        "classpath:static/img/",
                        "classpath:resources/templates/");
    }
}
