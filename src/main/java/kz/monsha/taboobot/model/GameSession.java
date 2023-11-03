package kz.monsha.taboobot.model;

import kz.monsha.taboobot.model.enums.GameSessionState;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class GameSession {
    private Long roomId;
    private LocalDateTime startedOn;
    private GameSessionState state;
    private List<GamerAccount> firstTeam;
    private List<GamerAccount> secondTeam;
    private GamerAccount creator;
    private Integer registrationMessageId;
    private Set<Long> playedCards;
}
