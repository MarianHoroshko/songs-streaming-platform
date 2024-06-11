package com.auth.demo;

import com.auth.demo.config.RSAKeysRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RSAKeysRecord.class)
@SpringBootApplication
public class DemoApplication {

//	TODO: generate new certs

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
