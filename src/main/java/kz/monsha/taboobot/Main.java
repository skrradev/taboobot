package kz.monsha.taboobot;


import kz.monsha.taboobot.config.MongoDBConfig;
import kz.monsha.taboobot.config.YamlConfigReader;
import kz.monsha.taboobot.model.GameRoom;
import kz.monsha.taboobot.model.GameSession;
import kz.monsha.taboobot.model.GamerAccount;
import kz.monsha.taboobot.repository.GameRoomRepository;
import kz.monsha.taboobot.repository.GameSessionRepository;
import kz.monsha.taboobot.repository.GamerAccountRepository;
import kz.monsha.taboobot.repository.GamerCardRepository;
import kz.monsha.taboobot.service.BotInputService;
import kz.monsha.taboobot.service.BotOutputService;
import kz.monsha.taboobot.service.GameService;
import kz.monsha.taboobot.service.TelegramApiService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        YamlConfigReader configReader = new YamlConfigReader("application.yml");
        String botToken = configReader.getValue("telegram.bot.token");
        String botUsername = configReader.getValue("telegram.bot.username");

        System.out.println("Bot Token: " + botToken);
        System.out.println("Bot Username: " + botUsername);


        MongoDBConfig mongoDBConfig = new MongoDBConfig(configReader);


        GameSessionRepository gameSessionRepository = new GameSessionRepository() {
            Map<Long, GameSession> map = new HashMap<>();
            long id = 0;

            @Override
            public GameSession getByRoomId(Long roomChatId) {
                return map.get(roomChatId);
            }

            @Override
            public void save(GameSession gameSession) {
                map.put(id, gameSession);
                id++;
            }
        };
        GameRoomRepository gameRoomRepository = new GameRoomRepository() {
            Map<Long, GameRoom> map = new HashMap<>();
            long id = 0;

            @Override
            public GameRoom getByChatId(Long chatId) {
                return map.get(chatId);
            }

            @Override
            public void save(GameRoom room) {
                map.put(id, room);
                id++;
            }
        };

        ExecutorService threadPool = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        GamerAccountRepository gamerAccountRepository = new GamerAccountRepository() {
            Map<Long, GamerAccount> map = new HashMap<>();
            long id = 0;

            @Override
            public GamerAccount getByUserId(long userId) {
                return map.get(userId);
            }

            @Override
            public void save(GamerAccount gamerAccount) {
                map.put(id, gamerAccount);
                id++;
            }
        };

        GamerCardRepository gamerCardRepository = new GamerCardRepository() {
        };


        BotOutputService producerService = new BotOutputService(botUsername, botToken);

        TelegramApiService telegramApiService = new TelegramApiService(producerService);
        GameService gameService = new GameService(gameSessionRepository,
                gameRoomRepository,
                telegramApiService,
                threadPool,
                scheduler,
                gamerAccountRepository,
                gamerCardRepository
        );
        BotInputService consumerService = new BotInputService(gameService, botUsername, botToken);

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(consumerService);
        telegramBotsApi.registerBot(producerService);


    }
}