package pro.sky.telegramBot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegramBot.entity.NotificationTask;
import pro.sky.telegramBot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Transactional
    public void save(String text, long chatId, LocalDateTime dateTime){
        notificationTaskRepository.save(new NotificationTask(text,  chatId, dateTime.truncatedTo(ChronoUnit.MINUTES)));
    }

}
