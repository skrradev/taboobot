package kz.monsha.taboobot.exeptions;

public abstract class AbstractException extends RuntimeException {
    public AbstractException(String message) {
        super(message);
    }
}
