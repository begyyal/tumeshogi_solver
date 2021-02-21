package begyyal.shogi.processor;

import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.BanContext;

public class SelfProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Self;
    
    private SelfProcessor() {
	super();
    }
    
    public BanContext[] spread(BanContext context) {
	
	var ban = context.getLatestBan();
	var motigoma = context.selfMotigoma;
	
	ban.search(s -> s.player() == PlayerType)
		.stream()
		.flatMap(s -> s.getTerritory()
			.stream()
			.map(v -> {
			    var newBan = ban.clone();
			    return newBan.advanceItte(s, v, PlayerType, motigoma) ? newBan : null; 
			}))
		.filter(b -> b != null)
		.map(b -> context.branch(b, null, PlayerType))
		.collect(SuperListGen.collect());
	
	// 持ち駒パターン
	
	return null;
    }
    
    public static SelfProcessor newi() {
	return new SelfProcessor();
    }
}
