package begyyal.shogi.def;

import java.util.Arrays;

import begyyal.commons.util.function.XStrings;

public enum Player {

    Self("x"),
    Opponent("y"),
    None("*");

    public final String id;

    private Player(String id) {
	this.id = id;
    }

    public static Player of(String id) {
	return Arrays.stream(Player.values())
	    .filter(p -> XStrings.equals(id, p.id))
	    .findFirst()
	    .orElse(null);
    }
}
