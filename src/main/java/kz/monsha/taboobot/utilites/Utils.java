package kz.monsha.taboobot.utilites;

import org.telegram.telegrambots.meta.api.objects.Message;

public class Utils {

    public static void ensurePublicChat(Message message) {
        if (!message.getChat().getType().equals("private")
                && !message.getChat().getType().equals("channel")) return;

        throw new IllegalStateException("This action can only be called from public chats.");
    }
}
