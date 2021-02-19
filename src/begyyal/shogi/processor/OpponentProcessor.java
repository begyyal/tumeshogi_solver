package begyyal.shogi.processor;

import begyyal.shogi.def.Player;
import begyyal.shogi.object.BanContext;

public class OpponentProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Opponent;
    
    private OpponentProcessor() {
	super();
    }
    
    public BanContext[] spread(BanContext context) {
	return null;
    }
    
    public static OpponentProcessor newi() {
	return new OpponentProcessor();
    }
}
