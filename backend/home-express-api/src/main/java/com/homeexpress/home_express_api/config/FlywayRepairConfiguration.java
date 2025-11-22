package com.homeexpress.home_express_api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "spring.flyway.enabled", havingValue = "true")
public class FlywayRepairConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayRepairConfiguration.class);

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(
            @Value("${app.flyway.repair-on-migrate:false}") boolean repairOnMigrate) {
        return flyway -> {
            if (repairOnMigrate) {
                LOGGER.info("Running Flyway repair before migration to keep schema history in sync.");
                flyway.repair();
            }
            flyway.migrate();
        };
    }
}
