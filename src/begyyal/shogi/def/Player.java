package begyyal.shogi.def;

import java.util.Arrays;

import begyyal.commons.util.function.XStrings;

public enum Player {

    Self("x", 0),
    Opponent("y", 1),
    None("*", 0);

    public final String id;
    public final int hashIndex;

    private Player(String id, int hashIndex) {
	this.id = id;
	this.hashIndex = hashIndex;
    }

    public static Player of(String id) {
	return Arrays.stream(Player.values())
	    .filter(p -> XStrings.equals(id, p.id))
	    .findFirst()
	    .orElse(null);
    }
}
