package org.genc.explore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurkerServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurkerServerApplication.class, args);
    }
}
