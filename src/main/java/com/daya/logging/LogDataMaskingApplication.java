package com.daya.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class LogDataMaskingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogDataMaskingApplication.class, args);
        LogDataMaskingApplication.maskingTest();
    }

    public static void maskingTest() {
        String data = "{\"loginName\":\"maskingtest\",\"phoneNumber\":\"9898981212\",\"password\":\"Masking@123\"}";
        log.info(data);
    }

}
