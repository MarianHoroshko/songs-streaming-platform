package com.auth.demo;

import com.auth.demo.config.RSAKeysRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableConfigurationProperties(RSAKeysRecord.class)
@EnableDiscoveryClient
@SpringBootApplication
public class DemoApplication {

//	TODO: generate new certs

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
