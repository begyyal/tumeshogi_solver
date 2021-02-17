package begyyal.shogi.processor;

import begyyal.shogi.def.Player;

public class OpponentProcessor extends ProcessorBase {

    private OpponentProcessor(String arg) {
	super(Player.Opponent, arg);
    }
    
    public static OpponentProcessor newi(String arg) {
	return new OpponentProcessor(arg);
    }
}
