package org.example.testcargobot.bot.runner;

import org.example.testcargobot.bot.entity.ServiceStatus;
import org.example.testcargobot.bot.entity.Services;
import org.example.testcargobot.bot.repo.ServiceRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServicesRunner implements CommandLineRunner {


    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl;
    private final ServiceRepo serviceRepo;

    @Override
    public void run(String... args)  {

        if (ddl.equals("create")) {
        Services service1 = Services.builder()
                .name("Easy Cargo")
                .url("https://back.ezycargo.uz/product/trackingid")
                .body("{ \"trackingid\": \"JT5216141955895\" }")
                .serviceStatus(ServiceStatus.ok)
                .build();
        serviceRepo.save(service1);
        }
    }
}
