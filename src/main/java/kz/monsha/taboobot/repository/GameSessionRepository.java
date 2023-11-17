package kz.monsha.taboobot.repository;

import kz.monsha.taboobot.model.GameSession;

import java.util.Optional;

public interface GameSessionRepository {
    Optional<GameSession> getByRoomId(Long roomChatId);

    void save(GameSession gameSession);

    Optional<GameSession> getByCreator(Long userId);

    void deleteByUserId(Long userId);
}
