package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.notification.PushNotificationDto;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNotificationService{

    public boolean sendPushNotification(PushNotificationDto pushRequest) {
        log.info("Sending push notification to user: {}", pushRequest.userId());
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(pushRequest.title())
                            .setBody(pushRequest.body())
                            .build())
                    .setToken(pushRequest.deviceToken())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);

            log.info("Push notification - Title: {}, Body: {}", pushRequest.title(), pushRequest.body());
            log.info("Push notification sent successfully to user: {}", pushRequest.userId());
            return true;
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
            return false;
        }
    }
}
