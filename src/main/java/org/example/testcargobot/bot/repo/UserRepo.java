package org.example.testcargobot.bot.repo;


import org.example.testcargobot.bot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserRepo extends JpaRepository<User,Integer> {
  User findByChatId(Long chatId);
  List<User> findAllByActive(Boolean active);
}
