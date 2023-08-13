package pro.sky.telegramBot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final Pattern PATTERN = Pattern.compile(
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2}) ([А-яA-z\\d,\\s.?!:]+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "d.M.yyyy HH:mm");

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long id = update.message().chat().id();
            if (update.message() != null){
                Message message = update.message();
                String text = message.text();
                if ("/start".equals(text)){
                    SendMessage sendMessage = new SendMessage(
                            id,
                            "Для планирования уведомлений пришлите сообщение в следующем формате: \\n*01.01.2022 20:00 Текст уведомления*"
                    );
                    sendMessage.parseMode(ParseMode.Markdown);
                    SendResponse sendResponse = telegramBot.execute(sendMessage);
                    if (!sendResponse.isOk()){
                        logger.error("Ошибка отправки сообщения: " + sendResponse.description());
                    }
                }
                if (text != null){
                    Matcher matcher = PATTERN.matcher(text);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private LocalDateTime parse(String dateTime){
        try{
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        }catch (DateTimeParseException e){
            return null;
        }

    }

}
