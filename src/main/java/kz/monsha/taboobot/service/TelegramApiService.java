package kz.monsha.taboobot.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@RequiredArgsConstructor
public class TelegramApiService {

    private final BotService botService;

    @SneakyThrows
    public Message sendMessage(SendMessage sendMessage) {
        return botService.execute(sendMessage);
    }

    @SneakyThrows
    public void sendSimpleMessage(SendMessage sendMessage) {
        botService.execute(sendMessage);
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

    @SneakyThrows
    private void execute(BotApiMethodBoolean deleteMessageAction) {
        botService.execute(deleteMessageAction);
    }
}
