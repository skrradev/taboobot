package kz.monsha.taboobot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRoom {
    private Long id;
    private String roomName;
    private Long roomChatId;

}
