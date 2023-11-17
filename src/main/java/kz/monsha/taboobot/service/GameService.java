package kz.monsha.taboobot.service;

import kz.monsha.taboobot.dto.RegistrationMessageData;
import kz.monsha.taboobot.exeptions.SimpleException;
import kz.monsha.taboobot.model.*;
import kz.monsha.taboobot.model.enums.GameCardEvent;
import kz.monsha.taboobot.model.enums.GameSessionState;
import kz.monsha.taboobot.repository.GameRoomRepository;
import kz.monsha.taboobot.repository.GameSessionRepository;
import kz.monsha.taboobot.repository.GamerAccountRepository;
import kz.monsha.taboobot.repository.GamerCardRepository;
import kz.monsha.taboobot.utilites.Utils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final GameRoomRepository gameRoomRepository;
    private final TelegramApiService telegramApiService;
    private final ExecutorService threadPool;
    private final ScheduledExecutorService scheduler;
    private final GamerAccountRepository gamerAccountRepository;
    private final GamerCardRepository gamerCardRepository;
    private final GameLifecycleManagerService gameLifecycleManagerService;

    public void processNewGameCommand(Message message) {
        Utils.ensurePublicChat(message, new SimpleException("This action can only be called from public chats."));

        User user = message.getFrom();
        GameRoom gameRoom = getOrCreateGameRoom(message);
        GamerAccount gamerAccount = gamerAccountRepository.getByUserId(user.getId());

        if (gamerAccount == null) {
            throw new SimpleException("You can't create a game! You should register at first! Register by /start command in DM with bot");
        }

        // trying to find existing session from cache
        var gameSessionOpt = gameSessionRepository.getByRoomId(gameRoom.getRoomChatId());

        if (gameSessionOpt.isPresent()) {
            telegramApiService.sendSimpleMessage(gameRoom.getRoomChatId(), "You can't create a game where it already exists :)");
            return;
        }

        GameSession gameSession = new GameSession();
        gameSession.setRoomId(gameRoom.getRoomChatId());
        gameSession.setState(GameSessionState.REGISTRATION);
        gameSession.setCreator(gamerAccount);

        gameSessionRepository.save(gameSession);

        createRegistrationMessage(gameSession);
    }

    private void createRegistrationMessage(GameSession gameSession) {

        Runnable runnable = () -> {
            try {

                int i = 0;
                while (true) {

                    log.info("Creating registration message");
                    Integer registrationMessageId = gameSession.getRegistrationMessageId();
                    if (registrationMessageId != null) {
                        telegramApiService.deleteMessage(gameSession.getRoomId(), registrationMessageId);
                    }

                    SendMessage sendMessageAction = new SendMessage(); // Create a message object
                    sendMessageAction.setChatId(String.valueOf(gameSession.getRoomId()));

                    RegistrationMessageData messageData = generateRegistrationMessage(gameSession);

                    sendMessageAction.setReplyMarkup(messageData.getMarkupInline());
                    sendMessageAction.setText(++i + messageData.getText());
                    sendMessageAction.setParseMode(ParseMode.HTML);

                    Message message = telegramApiService.sendMessage(sendMessageAction);
                    gameSession.setRegistrationMessageId(message.getMessageId());

                    gameSessionRepository.save(gameSession);

                    Thread.sleep(5000L);
//                    Thread.sleep(90_000L);

                    var optState = gameSessionRepository.getByRoomId(gameSession.getRoomId());

                    if (optState.isEmpty() || GameSessionState.REGISTRATION != optState.get().getState()) {
                        break;
                    }
                }

            } catch (Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
            }
        };

        threadPool.submit(runnable);

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
                                                
                        Team 1 players:
                        %s
                                                
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
        team1Button.setCallbackData("join_game?roomId=" + gameSession.getRoomId() + "&team=1");//TODO let's use json

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
        Optional<GameRoom> gameRoomOpt = gameRoomRepository.getByChatId(message.getChatId());
        if (gameRoomOpt.isPresent()) {
            return gameRoomOpt.get();
        }

        GameRoom room = new GameRoom();
        room.setRoomChatId(message.getChatId());
        room.setId(message.getChatId());
        room.setRoomName(message.getChat().getTitle());

        gameRoomRepository.save(room);
        return room;
    }


    private String resolveNickname(User user) {//TODO Simblify
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


    private void runGame(GameSession session) {
        int turns = 0;

        List<GamerAccount> teamOne = session.getFirstTeam();
        List<GamerAccount> teamTwo = session.getSecondTeam();

        int round = Math.max(teamOne.size(), teamTwo.size());

        for (int turn = 1; turn <= turns; turn++) {
            Iterator<GamerAccount> teamOneIter = teamOne.iterator();
            Iterator<GamerAccount> teamTwoIter = teamTwo.iterator();

            for (int j = 0; j < round; j++) {
                if (!teamOneIter.hasNext()) {
                    teamOneIter = teamOne.iterator();
                }
                GamerAccount memberFromOne = teamOneIter.next();

                if (!teamTwoIter.hasNext()) {
                    teamTwoIter = teamTwo.iterator();
                }
                GamerAccount memberFromTwo = teamTwoIter.next();

                String message = prepareMessageAboutRun(turn, memberFromOne, memberFromTwo);
                sendMessageToRoom(session.getRoomId(), message);
                checkReadiness(memberFromOne);
                sendCards(session, memberFromOne, memberFromTwo);
                validatePoints(session, memberFromOne, memberFromTwo);

                message = prepareMessageAboutRun(turn, memberFromTwo, memberFromOne);
                sendMessageToRoom(session.getRoomId(), message);
                checkReadiness(memberFromTwo);
                sendCards(session, memberFromTwo, memberFromOne);
                validatePoints(session, memberFromTwo, memberFromOne);
            }
        }
    }

    private void validatePoints(GameSession session, GamerAccount memberFromOne, GamerAccount memberFromTwo) {

    }


    Object someLock = new Object(); // TODO

    @SneakyThrows
    private void sendCards(GameSession session, GamerAccount giver, GamerAccount watcher) {
        Future<?> oneRunTask = threadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                GameCard gameCard = gameLifecycleManagerService.getNextCard();
                sendCardToGiver(gameCard, session, giver);
                sendCardToWatcher(gameCard, session, watcher);

                synchronized (someLock) {
                    try {
                        wait();
                        GameCardEvent gameCardEvent = session.getGameCardEvent();
                        switch (gameCardEvent) {
                            case NEXT -> session.getGuessedWords().add(gameCard.getWord());
                            case SKIP -> session.getSkippedWords().add(gameCard.getWord());
                            case BUZZER -> session.getBuzzerWords().add(gameCard.getWord());
                        }


                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }


        });


        scheduler.schedule(() -> {
            if (!oneRunTask.isDone()) {
                oneRunTask.cancel(true);
                System.out.println("Task was cancelled after a timeout.");
            }
        }, 1, TimeUnit.MINUTES);
    }

    private void sendCardToWatcher(GameCard gameCard, GameSession session, GamerAccount watcher) {
        String tabooWords = String.join("\n", gameCard.getTabooWords());

        String text = String.format(
                """
                        <b> %s </b>
                                           
                        %s   \s
                        """,
                gameCard.getWord(),
                tabooWords
        ).trim();

        if (session.getWatcherCardMessageId() != null) {
            telegramApiService.deleteMessage(watcher.getPersonalChatId(), session.getWatcherCardMessageId());
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton buzzerButton = new InlineKeyboardButton();
        buzzerButton.setText("Skip");
        buzzerButton.setCallbackData("buzzer?roomId=" + session.getRoomId());
        rowInline.add(buzzerButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        SendMessage sendMessageAction = new SendMessage(); // Create a message object
        sendMessageAction.setChatId(watcher.getPersonalChatId());
        sendMessageAction.setReplyMarkup(markupInline);
        sendMessageAction.setText(text);
        sendMessageAction.setParseMode(ParseMode.HTML);

        Message message = telegramApiService.sendMessage(sendMessageAction);

        session.setWatcherCardMessageId(message.getMessageId());
    }

    private void sendCardToGiver(GameCard gameCard, GameSession session, GamerAccount giver) {
        String tabooWords = String.join("\n", gameCard.getTabooWords());

        String text = String.format(
                """
                        <b> %s </b>
                                           
                        %s   \s
                        """,
                gameCard.getWord(),
                tabooWords
        ).trim();

        if (session.getGiverCardMessageId() != null) {
            telegramApiService.deleteMessage(giver.getPersonalChatId(), session.getGiverCardMessageId());
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton skipButton = new InlineKeyboardButton();
        skipButton.setText("Skip");
        skipButton.setCallbackData("skipCard?roomId=" + session.getRoomId());

        InlineKeyboardButton nextButton = new InlineKeyboardButton();
        nextButton.setText("Next");
        nextButton.setCallbackData("skipCard?roomId=" + session.getRoomId());

        rowInline.add(skipButton);
        rowInline.add(nextButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);


        SendMessage sendMessageAction = new SendMessage(); // Create a message object
        sendMessageAction.setChatId(giver.getPersonalChatId());
        sendMessageAction.setReplyMarkup(markupInline);
        sendMessageAction.setText(text);
        sendMessageAction.setParseMode(ParseMode.HTML);

        Message message = telegramApiService.sendMessage(sendMessageAction);

        session.setGiverCardMessageId(message.getMessageId());
    }

    private void checkReadiness(GamerAccount memberFromOne) {


    }

    private void sendMessageToRoom(Long roomId, String message) {

    }

    private String prepareMessageAboutRun(int round, GamerAccount memberFromOne, GamerAccount memberFromTwo) {

        return null;
    }


    public void processStartGameCommand(Message message) {
        Utils.validateIsPrivateChat(message, new SimpleException("this action can be made only in private message"));

        GameSession gameSession = gameSessionRepository.getByCreator(Utils.getUserId(message))
                .orElseThrow(() -> new SimpleException("You haven't created a game."));

        if (GameSessionState.REGISTRATION != gameSession.getState()) {
            throw new SimpleException("game not created yet or already started");
        }
        gameSession.setState(GameSessionState.PLAYING);//TODO or on preparing state
    }

    public void processStopGameCommand(Message message) {
        //gameSessionRepository.getByRoomId(message.get)
    }

    public void processLeaveGameCommand(Message message) {
        Utils.validateIsPrivateChat(message, new SimpleException("this action can be made only in private message"));
        gameSessionRepository.deleteByUserId(message.getFrom().getId());
    }

    public void processLeaderboardCommand(Message message) {


    }

    public void processRegistrationCommand(Message message) {
        if (!message.getChat().getType().equals("private"))//TODO Make util to check if it is private
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
        gamerAccount.setNickName(nickname);
        gamerAccount.setPersonalChatId(message.getChatId());
        gamerAccountRepository.save(gamerAccount);

    }

    public void senMessage(Long id, String message) {
        telegramApiService.sendSimpleMessage(id, message);
    }

    //we can move this logic to separate service
    public void processJoinGame(Long userId, CallBackParams callBackData) {

        Long roomId = callBackData.getLong("roomId");
        GameSession gameSession = gameSessionRepository.getByRoomId(roomId).orElseThrow(() -> new SimpleException("room not found!"));
        if (GameSessionState.REGISTRATION != gameSession.getState()) {
            return;
        }
        Long team = callBackData.getLong("team");

        gameSession.getFirstTeam().removeIf((itm) -> itm.getUserId().equals(userId));
        gameSession.getSecondTeam().removeIf((itm) -> itm.getUserId().equals(userId));

        GamerAccount gamerAccount = gamerAccountRepository.getByUserId(userId);
        if (team == 1) {
            gameSession.addToFirstTeam(gamerAccount);
        } else {
            gameSession.addToSecondTeam(gamerAccount);
        }

    }
}

