package begyyal.shogi.processor;

import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;

public class OpponentProcessor extends PlayerProcessorBase {

    private OpponentProcessor(String arg) {
	super(Player.Opponent, arg);
    }
    
    public Ban[] spread(Ban ban) {
	return null;
    }
    
    public static OpponentProcessor newi(String arg) {
	return new OpponentProcessor(arg);
    }
}
