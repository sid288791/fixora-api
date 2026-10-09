package org.simulynx.fixora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FixoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixoraApplication.class, args);
    }
}
