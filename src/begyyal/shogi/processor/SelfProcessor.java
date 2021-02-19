package begyyal.shogi.processor;

import begyyal.shogi.def.Player;
import begyyal.shogi.object.BanContext;

public class SelfProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Self;
    
    private SelfProcessor() {
	super();
    }
    
    public BanContext[] spread(BanContext context) {
	return null;
    }
    
    public static SelfProcessor newi() {
	return new SelfProcessor();
    }
}
