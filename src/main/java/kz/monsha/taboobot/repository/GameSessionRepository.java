package kz.monsha.taboobot.repository;

import kz.monsha.taboobot.model.GameSession;

public interface GameSessionRepository {
    GameSession getByRoomId(Long roomChatId);//TODO make  it return optional

    void save(GameSession gameSession);
}
