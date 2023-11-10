package kz.monsha.taboobot.utilites;

import org.telegram.telegrambots.meta.api.objects.Message;

public class Utils {

    public static void ensurePublicChat(Message message, RuntimeException runtimeException) {
        if (!message.getChat().getType().equals("private")
                && !message.getChat().getType().equals("channel")) return;
        throw runtimeException;
    }
}
