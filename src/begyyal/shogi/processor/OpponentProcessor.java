package begyyal.shogi.processor;

import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class OpponentProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Opponent;
    
    private OpponentProcessor() {
	super();
    }
    
    public BanContext[] spread(BanContext context) {
	
	var ban = context.getLatestBan();

//	var contextStream1 = ban.search(s -> s.player() == PlayerType)
//	    .flatMap(s -> spreadMasuState(s, ban)
//		.filter(isOute(ban))
//		.map(range -> Pair.of(s, range)))
//	    .map(s -> {
//		var newBan = ban.clone();
//		var k = newBan.advance(s.getLeft(), s.getRight(), PlayerType);
//		return context.branch(newBan, k, PlayerType, true);
//	    });
//
//	return Stream.concat(contextStream1, contextStream2).toArray(BanContext[]::new);
	return null;
    }
    
    public static OpponentProcessor newi() {
	return new OpponentProcessor();
    }
}
