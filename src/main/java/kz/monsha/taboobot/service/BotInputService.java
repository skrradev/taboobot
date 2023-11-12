package kz.monsha.taboobot.service;

import kz.monsha.taboobot.exeptions.AbstractException;
import kz.monsha.taboobot.model.CallBackParams;
import kz.monsha.taboobot.utilites.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RequiredArgsConstructor
public class BotInputService extends TelegramLongPollingBot {

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
        try {
            if (update.hasMessage()) {
                onMessageReceived(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                onCallbackQueryReceived(update.getCallbackQuery());
            }
        } catch (AbstractException e) {
            gameService.senMessage(update.getMessage().getChat().getId(), e.getMessage());
        }
    }

    private void onMessageReceived(Message message) {
        String text = message.getText();
        log.info("got message: {}", text);
        if (text.equalsIgnoreCase("/start")) {
            gameService.processRegistrationCommand(message);
        } else if (text.equalsIgnoreCase("/newGame")) {
            gameService.processNewGameCommand(message);
        } else if (text.equalsIgnoreCase("/startGame")) {
            gameService.processStartGameCommand(message);
        } else if (text.equalsIgnoreCase("/stopGame")) {// TODO explain what is the difference stop and leave
            gameService.processStopGameCommand(message);
        } else if (text.equalsIgnoreCase("/leave")) {
            gameService.processLeaveGameCommand(message);
        } else if (text.equalsIgnoreCase("/leaderboard")) {
            gameService.processLeaderboardCommand(message);
        }

    }


    private void onCallbackQueryReceived(CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        log.info(data);
        log.info("callback got {}", callbackQuery.getFrom().getId());
        CallBackParams callBackData = Utils.parseCallbackParams(data);

        switch (callBackData.getAction()) {

            case "join_game" -> gameService.processJoinGame(callbackQuery.getFrom().getId(), callBackData);
        }

    }


}
