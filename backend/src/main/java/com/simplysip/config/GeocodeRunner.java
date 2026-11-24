package com.simplysip.config;

import com.simplysip.service.NominatimGeocodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@ConditionalOnProperty(prefix = "app.geocode", name = "enabled", havingValue = "true")
public class GeocodeRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GeocodeRunner.class);

    private final NominatimGeocodeService nominatimGeocodeService;

    public GeocodeRunner(NominatimGeocodeService nominatimGeocodeService) {
        this.nominatimGeocodeService = nominatimGeocodeService;
    }

    @Override
    public void run(String... args) {
        log.info("app.geocode.enabled=true — filling missing latitude/longitude via Nominatim");
        nominatimGeocodeService.fillMissingCoordinates();
    }
}
