package org.openhospital.smartdoc.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the shared module.
 * Enables configuration properties for upload functionality.
 */
@Configuration
@ConfigurationPropertiesScan(value = "org.openhospital.smartdoc")
public class SharedConfig {
}