package kz.monsha.taboobot.dto;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Getter
@Setter
public class RegistrationMessageData {

    private final InlineKeyboardMarkup markupInline
            ;
    private final String text;

    public RegistrationMessageData(String text, InlineKeyboardMarkup markupInline) {
        this.text = text;
        this.markupInline = markupInline;
    }
}
