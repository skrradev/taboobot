package kz.monsha.taboobot.service;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {

    private final GameService gameService;
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
        if (update.hasMessage()) {
            onMessageReceived(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            onCallbackQueryReceived(update.getCallbackQuery());
        }
    }

    private void onMessageReceived(Message message) {
        String text = message.getText();

        if(text.startsWith("/start")) {
            gameService.processRegistrationCommand(message);
        } else if(text.startsWith("/newGame")) {
            gameService.processNewGameCommand(message);
        } else if(text.startsWith("/startGame")) {
            gameService.processStartGameCommand(message);
        } else if(text.startsWith("/stopGame")) {
            gameService.processStopGameCommand(message);
        } else if(text.startsWith("/leave")) {
            gameService.processLeaveGameCommand(message);
        } else if(text.startsWith("/leaderboard")) {
            gameService.processLeaderboardCommand(message);
        }

    }


    private void onCallbackQueryReceived(CallbackQuery callbackQuery) {


    }


}
