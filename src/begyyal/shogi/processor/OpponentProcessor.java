package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.Stream;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.object.SuperBool;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class OpponentProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Opponent;

    public OpponentProcessor(int numOfMoves) {
	super(numOfMoves);
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.getLatestBan();
	var opponentOu = ban.search(MasuState::isOpponentOu).findFirst().get();

	// 王手範囲から避ける(王による王手駒の取得含む)
	var cs1 = spreadMasuState(opponentOu, ban)
	    .filter(s -> !s.rangedBy
		.anyMatch(r -> ban.getState(r.getLeft(), r.getRight()).player != PlayerType))
	    .map(s -> {
		var newBan = ban.clone();
		var newState = newBan.advance(opponentOu.x, opponentOu.y, s.x, s.y, false);
		return checkingSafe(newBan, newState)
			? context.branch(newBan, newState, s.koma, PlayerType, true)
			: null;
	    });

	var outeArray = opponentOu.rangedBy
	    .stream()
	    .map(r -> ban.getState(r.getLeft(), r.getRight()))
	    .filter(s -> s.player != PlayerType)
	    .toArray(MasuState[]::new);
	if (outeArray.length > 1)
	    return cs1.filter(c -> c != null).toArray(BanContext[]::new);

	// 王手駒を取得する(王による取得は含まず)
	var outeState = outeArray[0];
	var cs2 = outeState.rangedBy
	    .stream()
	    .map(r -> ban.getState(r.getLeft(), r.getRight()))
	    .filter(s -> s.player == PlayerType && s.koma != Koma.Ou)
	    .flatMap(from -> {
		var tryNari = SuperBool.newi();
		return createBranchStream(outeState.y, from)
		    .filter(i -> tryNari.get()
			    || Ban.validateState(from.koma, outeState.x, outeState.y, PlayerType))
		    .mapToObj(i -> {
			var newBan = ban.clone();
			var newState = newBan.advance(
			    from.x, from.y, outeState.x, outeState.y, tryNari.getAndReverse());
			return checkingSafe(newBan)
				? context.branch(newBan, newState, outeState.koma, PlayerType, true)
				: null;
		    });
	    });

	// 持ち駒を貼る
	var outeVector = opponentOu.getVectorTo(outeState);
	boolean outeIsNotLinear = Math.abs(outeVector.x) == 1 || Math.abs(outeVector.y) == 1;
	var cs3 = context.opponentMotigoma.isEmpty() || outeIsNotLinear
		? Stream.empty()
		: Arrays.stream(MatrixResolver.decompose(outeVector))
		    .filter(miniV -> !outeVector.equals(miniV))
		    .flatMap(v -> context.opponentMotigoma
			.stream()
			.distinct()
			.map(k -> {
			    var newBan = ban.clone();
			    int x = opponentOu.x + v.x, y = opponentOu.y + v.y;
			    var s = newBan.deploy(k, x, y, PlayerType);
			    return s == MasuState.Invalid
				    ? null
				    : context.branch(newBan, s, k, PlayerType, false);
			}));

	return Stream.concat(Stream.concat(cs1, cs2), cs3)
	    .filter(c -> c != null)
	    .toArray(BanContext[]::new);
    }

    private static boolean checkingSafe(Ban ban) {
	return checkingSafe(ban, null);
    }

    private static boolean checkingSafe(Ban ban, MasuState ouState) {
	var ou = ouState == null
		? ban.search(MasuState::isOpponentOu).findFirst().get()
		: ouState;
	return ou.rangedBy
	    .stream()
	    .map(r -> ban.getState(r.getLeft(), r.getRight()))
	    .filter(s -> s.player != PlayerType)
	    .findFirst()
	    .isEmpty();
    }

    @Override
    protected Player getPlayerType() {
	return PlayerType;
    }
}
