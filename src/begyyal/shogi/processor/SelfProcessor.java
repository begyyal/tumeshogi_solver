package begyyal.shogi.processor;

import begyyal.shogi.def.Player;

public class SelfProcessor extends ProcessorBase {

    private SelfProcessor(String arg) {
	super(Player.Self, arg);
    }
    
    public static SelfProcessor newi(String arg) {
	return new SelfProcessor(arg);
    }
}
