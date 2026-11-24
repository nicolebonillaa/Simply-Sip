package com.simplysip;

import com.simplysip.config.GeocodeProperties;
import com.simplysip.config.ImportProperties;
import com.simplysip.config.RecommendationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ImportProperties.class, GeocodeProperties.class, RecommendationProperties.class})
public class SimplySipApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplySipApplication.class, args);
    }
}
