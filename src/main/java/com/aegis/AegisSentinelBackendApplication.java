package com.aegis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.aegis.identity.infrastructure.persistence.jpa")
public class AegisSentinelBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AegisSentinelBackendApplication.class, args);
    }

}
