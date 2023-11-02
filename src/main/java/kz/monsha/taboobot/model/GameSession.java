package kz.monsha.taboobot.model;

import java.time.LocalDateTime;
import java.util.List;

public class GameSession {
    private Long id;
    private Long roomId;
    private LocalDateTime startedOn;
    private LocalDateTime finishedOn;
    //private GameSessionStates state;
    private List<GameSessionMember> gameMembers;
    private GamerAccount createdByGamerAccount;
    private int rounds;

}
