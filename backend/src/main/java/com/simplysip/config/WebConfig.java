package com.simplysip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path dir = Path.of(uploadsDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(dir.toUri().toString());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create uploads directory", e);
        }
    }
}