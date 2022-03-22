package begyyal.shogi.def;

import java.util.Arrays;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.function.XStrings;

public enum Player {

    Self("x", "先手", 0),
    Opponent("y", "後手", 1),
    None("*", Strs.empty, 0);

    public final String id;
    public final String desc;
    public final int hashIndex;

    private Player(String id, String desc, int hashIndex) {
	this.id = id;
	this.desc = desc;
	this.hashIndex = hashIndex;
    }

    public static Player of(String id) {
	return Arrays.stream(Player.values())
	    .filter(p -> XStrings.equals(id, p.id))
	    .findFirst()
	    .orElse(null);
    }
}
