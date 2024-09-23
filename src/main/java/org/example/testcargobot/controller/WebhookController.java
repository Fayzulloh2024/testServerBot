package org.example.testcargobot.controller;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.example.testcargobot.bot.BotActions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final Gson gson = new Gson();
    private final BotActions botActions;

    @PostMapping("/")
    public String onUpdateReceived(@RequestBody String requestBody) {
        Update update = gson.fromJson(requestBody, Update.class);
        botActions.handle(update);
        return "Webhook received";
    }
}
