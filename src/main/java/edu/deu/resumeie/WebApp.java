package edu.deu.resumeie;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class WebApp {

    public static void main(String[] args) {
        System.out.println("SPRING MAIN");
    }
/*
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/rest/cities").allowedOrigins("http://localhost:4200/");
                registry.addMapping("/rest/unilist").allowedOrigins("http://localhost:4200/");
                registry.addMapping("/rest/languages").allowedOrigins("http://localhost:4200/");
                registry.addMapping("/rest/driverlicencetypes").allowedOrigins("http://localhost:4200/");
                registry.addMapping("/rest/educationdegrees").allowedOrigins("http://localhost:4200/");
            }
        };
    }
*/
}
