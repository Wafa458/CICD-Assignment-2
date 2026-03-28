package com.tus.order.karate;

import com.intuit.karate.junit5.Karate;
import com.tus.order.OrderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ApiKarateTest {

    private static ConfigurableApplicationContext context;

    @BeforeAll
    static void startApp() {
        System.setProperty("server.port", "8081");
        System.setProperty("spring.datasource.url", "jdbc:h2:mem:karate-db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        context = SpringApplication.run(OrderApplication.class);
    }

    @AfterAll
    static void stopApp() {
        if (context != null) {
            context.close();
        }
    }

    @Karate.Test
    Karate runApiTests() {
        return Karate.run("classpath:features");
    }
}

