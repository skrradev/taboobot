package kz.monsha.taboobot.utilites;

import kz.monsha.taboobot.model.CallBackParams;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
public class Utils {

    public static void ensurePublicChat(Message message, RuntimeException runtimeException) {
        if (!message.getChat().getType().equals("private")
                && !message.getChat().getType().equals("channel")) return;
        throw runtimeException;
    }


    public static CallBackParams parseCallbackParams(String data) {
        CallBackParams params = new CallBackParams();
        try {

            int actionIndex = data.indexOf('?');
            String action = data.substring(0, actionIndex);
            params.setAction(action);
            data = data.substring(actionIndex + 1);
            String[] paramPairs = data.split("&");

            for (String paramPair : paramPairs) {
                int i = paramPair.indexOf('=');
                String key = paramPair.substring(0, i);
                String val = paramPair.substring(i + 1);
                params.put(key, val);
            }

        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
        }
        return params;
    }

    public static void validateIsPrivateChat(Message message, RuntimeException e) {
        if (!message.getChat().getType().equals("private")) {
            throw e;
        }
    }

    public static Long getUserId(Message message) {
        return message.getFrom().getId();
    }
}
