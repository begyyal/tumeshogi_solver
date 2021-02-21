package begyyal.shogi.processor;

import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
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
			    var motigomaBucket = new Koma[1];
			    return newBan.advanceItte(s, v, PlayerType, motigomaBucket) 
				    ? context.branch(newBan, motigomaBucket[0], PlayerType, true) 
				    : null;
			}))
		.filter(b -> b != null)
		.collect(SuperListGen.collect());

	// 持ち駒パターン

	return null;
    }

    public static SelfProcessor newi() {
	return new SelfProcessor();
    }
}
