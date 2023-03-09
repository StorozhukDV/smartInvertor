package ru.smarteps.batterycontroller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BatteryControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatteryControllerApplication.class, args);
    }

}
