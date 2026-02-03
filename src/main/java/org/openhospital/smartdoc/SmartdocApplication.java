package org.openhospital.smartdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class SmartdocApplication {

    static void main(String[] args) {
        SpringApplication.run(SmartdocApplication.class, args);
    }

}
