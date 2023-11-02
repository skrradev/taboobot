package kz.monsha.taboobot.model;

public class GameRoom {
    private Long id;
    private String roomName;
    private Long roomId;

    // Constructors, Getters, and Setters
    public GameRoom() {}

    public GameRoom(Long id, String roomName, Long roomId) {
        this.id = id;
        this.roomName = roomName;
        this.roomId = roomId;
    }

    // Assume getters and setters for all fields are here
}
