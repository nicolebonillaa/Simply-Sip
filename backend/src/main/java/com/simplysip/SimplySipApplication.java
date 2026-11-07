package com.simplysip;

import com.simplysip.config.ImportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ImportProperties.class)
public class SimplySipApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplySipApplication.class, args);
    }
}
