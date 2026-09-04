package com.teamreports.weeklyreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WeeklyReportBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeeklyReportBackendApplication.class, args);
    }
}
