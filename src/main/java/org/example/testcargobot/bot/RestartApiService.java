package org.example.testcargobot.bot;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.testcargobot.bot.entity.DeleteMsg;
import org.example.testcargobot.bot.entity.ServiceStatus;
import org.example.testcargobot.bot.entity.Services;
import org.example.testcargobot.bot.entity.User;
import org.example.testcargobot.bot.repo.ServiceRepo;
import org.example.testcargobot.bot.repo.UserRepo;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestartApiService {


    private final List messageDeleteList;
    private final TelegramBot telegramBot;
    private final UserRepo userRepo;
    private final RestTemplate restTemplate;
    private final ServiceRepo serviceRepo;


    public void restartApiServer() {
        String host = "185.196.213.56"; // Server IP
        String user = "root";           // SSH foydalanuvchi
        String password = "1968971971aA!"; // SSH parol (parolingizni kiriting)
        int port = 22;                  // SSH porti (standart 22)

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);

            // SSH strict host key checking ni o'chirish
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();

            // Buyruqlarni ketma-ket bajarish
            executeCommand(session, "cd /var/www/backend-eazy && pm2 delete all");
            executeCommand(session, "pm2 save");
            executeCommand(session, "cd /var/www/backend-eazy && pm2 start ts-node -- -r tsconfig-paths/register --transpile-only -r dotenv/config src/server.ts --name backend-eazy");
            executeCommand(session, "pm2 save");
            executeCommand(session, "pm2 startup");

            session.disconnect();
            clearErorMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private void sendInfo() {
        Thread.sleep(60000);
        List<User> all = userRepo.findAllByActive(true);
            Services easyCargo = serviceRepo.findByName("Easy Cargo");
        if (checkServise() && easyCargo.getServiceStatus().equals(ServiceStatus.heal)) {
            easyCargo.setServiceStatus(ServiceStatus.ok);
            serviceRepo.save(easyCargo);
            String msg = "Ezy cargo qayta ishga tushdi✅" + "\n" +
                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            for (User user : all) {
                telegramBot.execute(new SendMessage(user.getChatId(), msg));
            }
        }


    }

    private boolean checkServise() {
      return   checkRestart("https://back.ezycargo.uz/product/trackingid",
                "Easy Cargo",
                "{ \"trackingid\": \"JT5216141955895\" }"
        );
    }

    private boolean checkRestart(String url, String serviceName, String body) {
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
            } else {
                return true;
            }
        } catch (HttpClientErrorException e) {
            // Http xatolarini boshqarish
            handleErrorResponse(serviceName, e.getStatusCode(), e.getResponseBodyAsString());

        } catch (Exception e) {
            // Boshqa xatoliklarni qayta ishlash
            handleErrorResponse(serviceName, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return false;

    }


    private void handleErrorResponse(String serviceName, HttpStatusCode statusCode, String body) {
        List<User> all = userRepo.findAllByActive(true);
        String msg = "Qayta ishga tushirishda xatolik❌ \n"+"Service: " + serviceName + "\n\nError: " + statusCode + "\n\nResponse Body: " + body;
        Services easyCargo = serviceRepo.findByName("Easy Cargo");
        easyCargo.setServiceStatus(ServiceStatus.error);
        serviceRepo.save(easyCargo);
        for (User user : all) {
            SendMessage sendMessage = new SendMessage(user.getChatId(), msg);
            telegramBot.execute(sendMessage);

        }

    }

    private void clearErorMsg() {
        for (Object o : messageDeleteList) {
            if (o instanceof DeleteMsg) {
                DeleteMsg deleteMsg = (DeleteMsg) o;
                DeleteMessage deleteMessage = new DeleteMessage(deleteMsg.getUserId(), deleteMsg.getMsgId());
                telegramBot.execute(deleteMessage);
            }
        }
        messageDeleteList.clear();
        sendInfo();
    }


    private void executeCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        channel.connect();

        channel.disconnect();
    }
}
