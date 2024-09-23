package org.example.testcargobot.test;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.example.testcargobot.bot.entity.*;
import org.example.testcargobot.bot.repo.ServiceRepo;
import org.example.testcargobot.bot.repo.UserRepo;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ScheduledTask {

    private final RestTemplate restTemplate;
    private final TelegramBot telegramBot;
    private final UserRepo userRepo;
    private final List messageDeleteList;
    private final ServiceRepo serviceRepo;

    @Scheduled(fixedRate = 120000)
    public void checkServices() {
        Services service = serviceRepo.findByName("Easy Cargo");
        if (service!=null && service.getServiceStatus().equals(ServiceStatus.ok) || service.getServiceStatus().equals(ServiceStatus.error)) {
            checkService(service.getUrl(),service.getName(),service.getBody());
        }
    }

    public void checkService(String url, String serviceName, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // To'g'ridan-to'g'ri String body ishlatamiz
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            // URLga POST so'rovi yuborilyapti
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // Javob kodi 200 dan boshqacha bo'lsa, handleErrorResponse chaqiriladi
            if (response.getStatusCode() != HttpStatus.OK) {
                handleErrorResponse(serviceName, response.getStatusCode(), response.getBody());
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            // Haddan tashqari ko'p so'rov holati (429 status)
            handleTooManyRequests(e, url, serviceName,body);
        } catch (HttpClientErrorException e) {
            // Http xatolarini boshqarish
            handleErrorResponse(serviceName, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            // Boshqa xatoliklarni qayta ishlash
            handleErrorResponse(serviceName, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }




    private void handleTooManyRequests(HttpClientErrorException.TooManyRequests e, String url, String serviceName, String body) {
        long retryAfter = 1;
        if (e.getResponseHeaders().containsKey("Retry-After")) {
            retryAfter = Long.parseLong(e.getResponseHeaders().getFirst("Retry-After"));
        }
        try {
            Thread.sleep(retryAfter * 1000);  // retryAfter soniyadan keyin qayta urinib ko'ring
            checkService(url , serviceName,body);  // Keyin yana urinib ko'ring
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }




    private void handleErrorResponse(String serviceName, HttpStatusCode statusCode, String responseBody) {
        Services easyCargo = serviceRepo.findByName("Easy Cargo");
        if (easyCargo.getServiceStatus().equals(ServiceStatus.ok)) {
        easyCargo.setServiceStatus(ServiceStatus.error);
        serviceRepo.save(easyCargo);
        }
        List<User> all = userRepo.findAllByActive(true);
        String msg = "Service: " + serviceName + "\nError: " + statusCode + "\nResponse Body: " + responseBody;
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Restart " + serviceName + " API").callbackData("RestartApi"));
        for (User user : all) {
            SendMessage sendMessage = new SendMessage(user.getChatId(), msg);
            sendMessage.replyMarkup(inlineKeyboardMarkup);
            SendResponse execute = telegramBot.execute(sendMessage);
            DeleteMsg deleteMsg = new DeleteMsg(user.getChatId(), execute.message().messageId());
            messageDeleteList.add(deleteMsg);
        }
    }
}
