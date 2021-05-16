package begyyal.shogi.exception;

public class BanIllegalArgException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public BanIllegalArgException() {
	super("Ban argument format is invalid.");
    }
}