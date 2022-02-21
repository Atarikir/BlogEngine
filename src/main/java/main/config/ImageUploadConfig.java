package main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class ImageUploadConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/upload/**", "/js/**", "/css/**", "/fonts/**", "/img/**",
                        "/templates/**", "/post/upload/**", "/edit/upload/**")
                .addResourceLocations(
                        "file:upload/",
                        "classpath:static/js/",
                        "classpath:static/css/",
                        "classpath:static/fonts/",
                        "classpath:static/img/",
                        "classpath:resources/templates/");
    }
}
