package kz.monsha.taboobot.repository;

import kz.monsha.taboobot.model.GameRoom;

public interface GameRoomRepository {
    GameRoom getByChatId(Long chatId);

    void save(GameRoom room);
}
