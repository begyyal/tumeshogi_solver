package begyyal.shogi.def;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum Player {
    
    Self("x"),
    Opponent("y");
    
    private final String id;
    
    private Player(String id) {
	this.id = id;
    }
    
    public static Player of(String id) {
	return Arrays.asList(Player.values())
		.stream()
		.filter(p -> StringUtils.equals(id, p.id))
		.findFirst()
		.orElse(null);
    }
}
