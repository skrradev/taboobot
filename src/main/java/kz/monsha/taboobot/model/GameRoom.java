package kz.monsha.taboobot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRoom {
    private Long id;
    private String roomName;
    @Deprecated //let's use id which will contain value of ChatId
    private Long roomChatId;

}
