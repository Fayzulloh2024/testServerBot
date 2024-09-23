package org.example.testcargobot.bot.repo;


import org.example.testcargobot.bot.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepo extends JpaRepository<Services, Integer> {
    Services findByName(String name);
}

