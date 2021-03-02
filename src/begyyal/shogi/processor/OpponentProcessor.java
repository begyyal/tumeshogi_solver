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

	// !!! ベクトル方向は反転させる必要アリ
	
	// 王手範囲から避ける
	var opponentOu = ban
		.search(s -> s.koma() == Koma.Ou && s.player() == PlayerType)
		.findFirst().get();
	var contextStream1 = spreadMasuState(opponentOu, ban)
		.filter(s -> !s.rangedBy().anyMatch(s2 -> s2.player() != PlayerType))
		.map(s -> {
		    var newBan = ban.clone();
		    var k = newBan.advance(opponentOu, s, PlayerType);
		    return context.branch(newBan, s, opponentOu, k, PlayerType, true);
		});
	
	// 王手駒を取得する
//	var contextStream2 = opponentOu
	
//	return Stream.concat(contextStream1, contextStream2).toArray(BanContext[]::new);
	return null;
    }
    
    @Override
    protected Player getPlayerType() {
	return PlayerType;
    }
    
    public static OpponentProcessor newi() {
	return new OpponentProcessor();
    }
}
