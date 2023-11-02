package kz.monsha.taboobot.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerAccount {
    private Long id;
    private Long idExternal;
    private String nickName;
    private Long personalRoomId;

    // Constructors, Getters, and Setters
    public GamerAccount() {}

    public GamerAccount(Long id, Long idExternal, String nickName, Long personalRoomId) {
        this.id = id;
        this.idExternal = idExternal;
        this.nickName = nickName;
        this.personalRoomId = personalRoomId;
    }

    // Assume getters and setters for all fields are here
}
