package kz.monsha.taboobot.model;

import kz.monsha.taboobot.model.enums.GameCardEvent;
import kz.monsha.taboobot.model.enums.GameSessionState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class GameSession {
    private Long roomId;
    private LocalDateTime startedOn;
    private GameSessionState state;

    @Setter(AccessLevel.NONE)
    private List<GamerAccount> firstTeam = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private List<GamerAccount> secondTeam = new ArrayList<>();

    private GamerAccount creator;
    private Integer registrationMessageId;
    private Set<Long> playedCards;

    private volatile GameCardEvent gameCardEvent;


    private List<String> guessedWords = new ArrayList<>();
    private List<String> skippedWords = new ArrayList<>();
    private List<String> buzzerWords = new ArrayList<>();


    private Integer giverCardMessageId; // TODO
    private Integer watcherCardMessageId; // TODO

    public void addToFirstTeam(GamerAccount gamerAccount) {
        firstTeam.add(gamerAccount);
    }

    public void addToSecondTeam(GamerAccount gamerAccount) {
        secondTeam.add(gamerAccount);
    }
}
