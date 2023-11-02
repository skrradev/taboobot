package kz.monsha.taboobot.service;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {

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


    }
}
