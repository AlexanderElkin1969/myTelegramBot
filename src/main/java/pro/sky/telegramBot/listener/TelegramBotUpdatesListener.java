package pro.sky.telegramBot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegramBot.service.NotificationTaskService;
import pro.sky.telegramBot.service.TelegramBotService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final Pattern PATTERN = Pattern.compile(
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2}) ([А-яA-z\\d,\\s.?!:]+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "dd.MM.yyyy HH:mm");

    private static final String TEXT_MESSAGE = "Для планирования уведомлений пришлите сообщение в следующем формате: \n*01.01.2022 20:00 Текст уведомления*";

    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    private final TelegramBotService telegramBotService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService, TelegramBotService telegramBotService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.telegramBotService = telegramBotService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                LocalDateTime dateTime;
                Long id = update.message().chat().id();
                if (update.message() != null) {
                    String text = update.message().text();
                    if ("/start".equals(text)) {
                        telegramBotService.sendMessage(id, TEXT_MESSAGE, ParseMode.Markdown);
                    } else if (Objects.nonNull(text)) {
                        Matcher matcher = PATTERN.matcher(text);
                        if (matcher.find()) {
                            dateTime = parse(matcher.group(1));
                            if (Objects.nonNull(dateTime)) {
                                notificationTaskService.save(matcher.group(2), id, dateTime);
                                telegramBotService.sendMessage(id, "Вы получите уведомление " + dateTime);
                            } else {
                                telegramBotService.sendMessage(id, "Invalid date and time format.");
                            }
                        } else {
                            telegramBotService.sendMessage(id, "I'm sorry I do not understand you.");
                        }
                    }else {
                        telegramBotService.sendMessage(id, "I'm sorry I do not understand you.");
                    }
                }
            });
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String dateTime){
        try{
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        }catch (DateTimeParseException e){
            return null;
        }

    }

}
