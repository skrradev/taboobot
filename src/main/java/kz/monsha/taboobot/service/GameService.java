package kz.monsha.taboobot.service;

import kz.monsha.taboobot.dto.RegistrationMessageData;
import kz.monsha.taboobot.model.GameRoom;
import kz.monsha.taboobot.model.GameSession;
import kz.monsha.taboobot.model.GameSessionMember;
import kz.monsha.taboobot.model.GamerAccount;
import kz.monsha.taboobot.model.enums.GameSessionState;
import kz.monsha.taboobot.repository.GameRoomRepository;
import kz.monsha.taboobot.repository.GameSessionRepository;
import kz.monsha.taboobot.repository.GamerAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Slf4j
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final GameRoomRepository gameRoomRepository;
    private final TelegramApiService telegramApiService;
    private final ExecutorService threadPool;
    private final GamerAccountRepository gamerAccountRepository;

    public void processNewGameCommand(Message message) {
        ensurePublicChat(message);

        User user = message.getFrom();
        GameRoom gameRoom = getOrCreateGameRoom(message);
        GamerAccount gamerAccount = gamerAccountRepository.getByUserId(user.getId());

        if (gamerAccount == null) {
            throw new IllegalStateException("You can't create a game! You should register at first! Register by /start command in DM with bot");
        }

        // trying to find existing session from cache
        GameSession gameSession = gameSessionRepository.getByRoomId(gameRoom.getRoomChatId());

        if (gameSession != null) {
            telegramApiService.sendSimpleMessage(gameRoom.getRoomChatId(), "You can't create a game where it already exists :)");
            return;
        }

        gameSession = new GameSession();
        gameSession.setRoomId(gameRoom.getRoomChatId());
        gameSession.setState(GameSessionState.REGISTRATION);
        gameSession.setCreator(gamerAccount);

        gameSessionRepository.save(gameSession);

        createRegistrationMessage(gameSession);
    }

    private void createRegistrationMessage(GameSession gameSession) {

        Future<?> registrationMessageTask = threadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    log.info("Creating registration message");
                    Integer registrationMessageId = gameSession.getRegistrationMessageId();
                    if (registrationMessageId != null) {
                        telegramApiService.deleteMessage(gameSession.getRoomId(), registrationMessageId);
                        Thread.sleep(1000);
                    }

                    SendMessage sendMessageAction = new SendMessage(); // Create a message object
                    sendMessageAction.setChatId(String.valueOf(gameSession.getRoomId()));

                    RegistrationMessageData messageData = generateRegistrationMessage(gameSession);

                    sendMessageAction.setReplyMarkup(messageData.getMarkupInline());
                    sendMessageAction.setText(messageData.getText());
                    sendMessageAction.setParseMode(ParseMode.HTML);

                    Message message = telegramApiService.sendMessage(sendMessageAction);
                    gameSession.setRegistrationMessageId(message.getMessageId());

                    gameSessionRepository.save(gameSession);

                    Thread.sleep(90_000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Thread interrupted", e);
                    break;
                } catch (Exception e) {
                    log.error("Error occurred in registration message creation", e);
                }
            }
        });

    }


    private RegistrationMessageData generateRegistrationMessage(GameSession gameSession) {
        StringBuilder team1players = new StringBuilder();
        List<GamerAccount> team1Members = gameSession.getFirstTeam();
        for (int i = 0; i < team1Members.size(); i++) {
            GamerAccount member = team1Members.get(i);
            team1players.append(String.format("%d. %s \n", i + 1, member.getNickName()));
        }

        StringBuilder team2players = new StringBuilder();
        List<GamerAccount> team2Members = gameSession.getSecondTeam();

        for (int i = 0; i < team2Members.size(); i++) {
            GamerAccount member = team2Members.get(i);
            team2players.append(String.format("%d. %s \n", i + 1, member.getNickName()));
        }

        String text = String.format(
                """
                        <b>Welcome to Taboo game!!! </b>
                        Registration to a new game. Host is %s.
                        <br>
                        Team 1 players:
                        %s
                        <br>
                        Team 2 players:
                        %s""",
                gameSession.getCreator().getNickName(),
                team1players,
                team2players
        ).trim();

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton team1Button = new InlineKeyboardButton();
        team1Button.setText("Join team 1 🎮");
        team1Button.setCallbackData("join_game?roomId=" + gameSession.getRoomId() + "&team=1");

        InlineKeyboardButton team2Button = new InlineKeyboardButton();
        team2Button.setText("Join team 2 🎮");
        team2Button.setCallbackData("join_game?roomId=" + gameSession.getRoomId() + "&team=2");

        rowInline.add(team1Button);
        rowInline.add(team2Button);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        return new RegistrationMessageData(text, markupInline);
    }


    public GameRoom getOrCreateGameRoom(Message message) {
        GameRoom room = gameRoomRepository.getByChatId(message.getChatId());
        if (room != null) {
            return room;
        }
        room = new GameRoom();
        room.setRoomChatId(message.getChatId());
        room.setRoomName(message.getChat().getTitle());

        gameRoomRepository.save(room);
        return room;
    }


    private String resolveNickname(User user) {
        String nickName;
        if (user.getFirstName() != null && user.getLastName() != null) {
            nickName = user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            nickName = user.getFirstName();
        } else if (user.getLastName() != null) {
            nickName = user.getLastName();
        } else {
            nickName = user.getUserName();
        }
        return nickName;
    }


    private void ensurePublicChat(Message message) {
        if (!message.getChat().getType().equals("private")
                && !message.getChat().getType().equals("channel")) return;

        throw new IllegalStateException("Это действие разрешено вызывать только из публичных чатов");
    }


    public void processStartGameCommand(Message message) {


    }

    public void processStopGameCommand(Message message) {

    }

    public void processLeaveGameCommand(Message message) {


    }

    public void processLeaderboardCommand(Message message) {


    }

    public void processRegistrationCommand(Message message) {
        if (!message.getChat().getType().equals("private"))
            return;

        User user = message.getFrom();
        GamerAccount gamerAccount = gamerAccountRepository.getByUserId(user.getId());

        if (gamerAccount != null) {
            updateGamerAccountIfNeeded(message, gamerAccount);
            telegramApiService.sendSimpleMessage(message.getChatId(), "Thanks! You have been already registered!");
            return;
        }

        String nickname = resolveNickname(user);
        gamerAccount = new GamerAccount();
        gamerAccount.setUserId(user.getId());
        gamerAccount.setNickName(nickname);
        gamerAccount.setPersonalChatId(message.getChatId());
        gamerAccountRepository.save(gamerAccount);

        telegramApiService.sendSimpleMessage(message.getChatId(), "Thanks! You are registered! You can play now!");
    }

    private void updateGamerAccountIfNeeded(Message message, GamerAccount gamerAccount) {
        String nickname = resolveNickname(message.getFrom());
        if (!gamerAccount.getNickName().equals(nickname)) {
            gamerAccount.setNickName(nickname);
        }

        if (!gamerAccount.getPersonalChatId().equals(message.getChatId())) {
            gamerAccount.setPersonalChatId(message.getChatId());
        }

        gamerAccountRepository.save(gamerAccount);

    }
}