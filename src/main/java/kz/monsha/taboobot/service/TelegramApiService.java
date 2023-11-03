package kz.monsha.taboobot.service;


import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
public class TelegramApiService {

    private final BotService botService;


    public Message sendMessage(SendMessage sendMessage) {
        try {
            return botService.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendSimpleMessage(SendMessage sendMessage) {
        try {
            botService.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendSimpleMessage(Long chatId, String text) {
        var sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(text);
        sendMessage(sendMessageRequest);
    }



    public void deleteMessage(Long roomId, Integer registrationMessageId) {
        DeleteMessage deleteMessageAction = new DeleteMessage();
        deleteMessageAction.setChatId(roomId);
        deleteMessageAction.setMessageId(registrationMessageId);
        execute(deleteMessageAction);

    }

    private void execute(BotApiMethodBoolean deleteMessageAction) {
        try {
            botService.execute(deleteMessageAction);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
