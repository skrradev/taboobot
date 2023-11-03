package kz.monsha.taboobot.service;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@RequiredArgsConstructor
public class BotOutputService extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("This method must not be called");
        throw new RuntimeException("This method must not be called");
    }

}
