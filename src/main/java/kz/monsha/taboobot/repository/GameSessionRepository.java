package kz.monsha.taboobot.repository;

import kz.monsha.taboobot.model.GameSession;

public interface GameSessionRepository {
    GameSession getByRoomId(Long roomChatId);

    void save(GameSession gameSession);
}
