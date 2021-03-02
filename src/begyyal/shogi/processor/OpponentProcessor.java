package begyyal.shogi.processor;

import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class OpponentProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Opponent;

    private OpponentProcessor() {
	super();
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.getLatestBan();

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
	var contextStream2 = getContextStreamToEraseOute(ban, context, opponentOu);

	return Stream.concat(contextStream1, contextStream2)
	    .distinct()
	    .toArray(BanContext[]::new);
    }

    private Stream<BanContext> getContextStreamToEraseOute(
	Ban ban,
	BanContext context,
	MasuState opponentOu) {

	var outeStream = opponentOu
	    .rangedBy()
	    .stream()
	    .filter(s -> s.player() != PlayerType);
	if (outeStream.count() > 1)
	    return Stream.empty();

	var outeState = outeStream.findFirst().get();
	return outeState.rangedBy()
	    .stream()
	    .filter(s -> s.player() == PlayerType)
	    .map(s -> Pair.of(s, this.occupy(s, outeState)))
	    .map(sp -> {
		var to = sp.getRight();
		var newBan = ban.clone();
		var k = newBan.advance(sp.getLeft(), to, PlayerType);
		return newBan.validateState(to)
			? context.branch(newBan, to, sp.getLeft(), k, PlayerType, true)
			: null;
	    })
	    .filter(c -> c != null);
    }

    @Override
    protected Player getPlayerType() {
	return PlayerType;
    }

    public static OpponentProcessor newi() {
	return new OpponentProcessor();
    }
}
