package begyyal.shogi.processor;

import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;

public class SelfProcessor extends PlayerProcessorBase {

    private SelfProcessor(String arg) {
	super(Player.Self, arg);
    }
    
    public Ban[] spread(Ban ban) {
	return null;
    }
    
    public static SelfProcessor newi(String arg) {
	return new SelfProcessor(arg);
    }
}
