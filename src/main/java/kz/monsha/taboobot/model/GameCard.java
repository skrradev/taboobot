package kz.monsha.taboobot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameCard {
    private Long id;
    private String word;
    private List<String> tabooWords;
}
