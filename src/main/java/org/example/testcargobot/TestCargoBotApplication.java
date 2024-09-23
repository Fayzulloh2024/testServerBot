package org.example.testcargobot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import org.example.testcargobot.bot.entity.DeleteMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class TestCargoBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCargoBotApplication.class, args);
    }
    @Value("${bot.token}")
    private String token;


    @Value("${bot.webhookUrl}")
    private String webhookUrl;

    @Bean
    public TelegramBot telegramBot() {
        TelegramBot bot = new TelegramBot(token);
        bot.execute(new SetWebhook().url(webhookUrl));  // Webhookni o'rnatish
        return bot;
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    @Bean
    @Scope("singleton") // Ob'ekt faqat bir marta yaratiladi (singleton scope)
    public List<DeleteMsg> messageDeleteList() {
        return new ArrayList<>(); // Yangi bo'sh ArrayList<Integer>
    }


}
