package com.adplatform.eventlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EventLog Service Application
 * - 광고 이벤트 로깅 서비스 (Impression, Click, Conversion)
 */
@SpringBootApplication
public class EventLogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventLogServiceApplication.class, args);
    }
}
