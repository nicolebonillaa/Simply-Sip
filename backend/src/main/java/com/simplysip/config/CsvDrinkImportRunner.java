package com.simplysip.config;

import com.simplysip.service.CsvDrinkImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@ConditionalOnProperty(prefix = "app.import", name = "enabled", havingValue = "true")
public class CsvDrinkImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CsvDrinkImportRunner.class);

    private final CsvDrinkImportService csvDrinkImportService;

    public CsvDrinkImportRunner(CsvDrinkImportService csvDrinkImportService) {
        this.csvDrinkImportService = csvDrinkImportService;
    }

    @Override
    public void run(String... args) {
        log.info("app.import.enabled=true — starting CSV drink import");
        csvDrinkImportService.importAll();
    }
}
