package begyyal.shogi.def;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum Player {
    
    Self("x"),
    Opponent("y");
    
    public final String id;
    
    private Player(String id) {
	this.id = id;
    }
    
    public static Player of(String id) {
	return Arrays.stream(Player.values())
		.filter(p -> StringUtils.equals(id, p.id))
		.findFirst()
		.orElse(null);
    }
}
