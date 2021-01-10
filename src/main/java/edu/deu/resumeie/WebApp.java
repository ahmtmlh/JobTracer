package edu.deu.resumeie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class WebApp {

    public static void main(String[] args) {
        System.out.println("SPRING MAIN");
    }

    public static void start(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/rest/*")
                        .allowedMethods("GET", "POST")
                        .allowedOrigins("http://localhost:4200");
            }
        };
    }

}
