package kz.monsha.taboobot.repository;

import kz.monsha.taboobot.model.GameRoom;

import java.util.Optional;

public interface GameRoomRepository {
    Optional<GameRoom> getByChatId(Long chatId);

    void save(GameRoom room);
}
