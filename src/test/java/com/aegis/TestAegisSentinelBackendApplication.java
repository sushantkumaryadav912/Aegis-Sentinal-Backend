package com.aegis;

import org.springframework.boot.SpringApplication;

public class TestAegisSentinelBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(AegisSentinelBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
