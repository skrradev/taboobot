package kz.monsha.taboobot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerAccount {
    private Long id;
    private Long userId;
    private String nickName;
    private Long personalChatId;
}
