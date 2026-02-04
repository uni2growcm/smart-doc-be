package org.openhospital.smartdoc.modules.shared;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the shared module.
 * Enables configuration properties for upload functionality.
 */
@Configuration
@EnableConfigurationProperties(UploadProperties.class)
public class SharedConfiguration {
}