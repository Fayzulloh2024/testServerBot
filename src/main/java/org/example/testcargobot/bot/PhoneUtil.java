package org.example.testcargobot.bot;

import org.springframework.stereotype.Service;

@Service
public class PhoneUtil {

    public  String repairPhone(String phone) {
        if (phone.startsWith("+")) {
            return phone;
        } else {
            return "+" + phone;
        }
    }



}
