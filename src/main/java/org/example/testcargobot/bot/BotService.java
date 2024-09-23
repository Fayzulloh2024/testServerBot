package org.example.testcargobot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.testcargobot.bot.entity.TelegramStatus;
import org.example.testcargobot.bot.entity.User;
import org.example.testcargobot.bot.repo.UserRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class BotService {

    private final UserRepo userRepo;
    private final TelegramBot telegramBot;
    private final PhoneUtil phoneUtil;


    @SneakyThrows
    public void startMethod(Update update) {
        for (User user : userRepo.findAll()) {
            if (user.getChatId().equals(update.message().from().id())) {
                return;
            }
        }
        User user = new User();
        user.setChatId(update.message().from().id());
        user.setActive(false);
        user.setStatus(TelegramStatus.SHARING_CONTACT);
        userRepo.save(user);
        SendMessage sendMessage=new SendMessage(user.getChatId(),"\"Telefon raqamingizni +998XXXXXXXXX ko‘rinishida yuboring yoki <b>Raqamni yuborish</b> tugmasini bosish orqali telegram raqamingizni yuboring!\"").parseMode(com.pengrad.telegrambot.model.request.ParseMode.HTML);;
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(new KeyboardButton("Raqamni yuborish").requestContact(true)).resizeKeyboard(true);
        sendMessage.replyMarkup(replyKeyboardMarkup);
         telegramBot.execute(sendMessage);


    }




    public void acceptPhone(Update update) {
        User byChatId = userRepo.findByChatId(update.message().from().id());
        if (update.message().text()!=null) {
        byChatId.setPhone(phoneUtil.repairPhone(update.message().text()));
        }else {
            byChatId.setPhone(phoneUtil.repairPhone(update.message().contact().phoneNumber()));
        }
        byChatId.setStatus(TelegramStatus.ok);
        userRepo.save(byChatId);
        SendMessage sendMessage=new SendMessage(byChatId.getChatId(),"\uD83D\uDC64 Admin bo‘lish uchun so'rov yuborildi!");
        telegramBot.execute(sendMessage);
    }
}

