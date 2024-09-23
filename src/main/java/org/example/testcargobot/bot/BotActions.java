package org.example.testcargobot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import org.example.testcargobot.bot.entity.ServiceStatus;
import org.example.testcargobot.bot.entity.Services;
import org.example.testcargobot.bot.entity.TelegramStatus;
import org.example.testcargobot.bot.entity.User;
import org.example.testcargobot.bot.repo.ServiceRepo;
import org.example.testcargobot.bot.repo.UserRepo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.pengrad.telegrambot.model.Update;

import java.util.List;


@RequiredArgsConstructor
@Service
public class BotActions {

    private final BotService botService;
    private final RestartApiService restartApiService;
    private final UserRepo userRepo;
    private final TelegramBot telegramBot;
    private final ServiceRepo serviceRepo;

    @Async
    @SneakyThrows
    public void handle(Update update) {

        if (update.message() != null) {
            if (update.message().text()!=null) {
                var user = userRepo.findByChatId(update.message().from().id());
                if (update.message().text().equals("/start")) {
                    botService.startMethod(update);
                }
                else if (user == null || user.getStatus() == null) {
                    telegramBot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
                } else if (user.getStatus().equals(TelegramStatus.SHARING_CONTACT)) {
                    String messageText = update.message().text();
                    if (messageText != null && messageText.startsWith("+998") && messageText.length() == 13 && messageText.matches("\\+998\\d{9}")) {
                        botService.acceptPhone(update);
                    } else {
                        telegramBot.execute(new SendMessage(update.message().chat().id(), "Iltimos, raqamingizni namunadagidek kiriting "));
                        telegramBot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
                    }
                }
                else {
                    telegramBot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
                }
            }
            else if (userRepo.findByChatId(update.message().from().id())!=null && update.message().contact() != null && userRepo.findByChatId(update.message().from().id()).getStatus().equals(TelegramStatus.SHARING_CONTACT)) {
                botService.acceptPhone(update);
            }else {
                telegramBot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
            }
        } else if (update.callbackQuery().data().equals("RestartApi")) {
            Services service = serviceRepo.findByName("Easy Cargo");
            if (service.getServiceStatus().equals(ServiceStatus.error)) {
                service.setServiceStatus(ServiceStatus.heal);
                serviceRepo.save(service);
                List<User> users = userRepo.findAllByActive(true);
                for (User user : users) {
                telegramBot.execute(new SendMessage(user.getChatId(), "🔃Servisni qayta ishga tushirish boshlandi! \n Iltimos kuting⏳"));
                }
                restartApiService.restartApiServer();
            }
        }
    }
}
