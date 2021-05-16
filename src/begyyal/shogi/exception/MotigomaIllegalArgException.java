package begyyal.shogi.exception;

public class MotigomaIllegalArgException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public MotigomaIllegalArgException() {
	super("Motigoma argument format is invalid.");
    }
}