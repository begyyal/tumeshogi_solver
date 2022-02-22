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

    public static final Player playerType = Player.Opponent;
    private static final BanContext[] dummyResult = new BanContext[] { null };

    public OpponentProcessor(int numOfMoves) {
	super(numOfMoves);
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.getLatestBan();
	var opponentOu = ban.search(MasuState::isOpponentOu).findFirst().get();

	// 王手範囲から避ける(王による王手駒の取得含む)
	Stream<BanContext> cs1 = spreadMasuState(opponentOu, ban)
	    .filter(s -> checkingSafe(ban, s))
	    .map(s -> {
		var newBan = ban.clone();
		var newState = newBan.advance(opponentOu.x, opponentOu.y, s.x, s.y, false);
		return checkingSafe(newBan, newState)
			? context.branch(newBan, newState, s.koma, playerType, true)
			: null;
	    });

	var outeArray = opponentOu.rangedBy
	    .stream()
	    .map(r -> ban.getState(r.getLeft(), r.getRight()))
	    .filter(s -> s.player != playerType)
	    .toArray(MasuState[]::new);
	if (outeArray.length > 1)
	    return executeCS(context, cs1);

	// 王手駒を取得する(王による取得は含まず)
	var outeState = outeArray[0];
	Stream<BanContext> cs2 = outeState.rangedBy
	    .stream()
	    .map(r -> ban.getState(r.getLeft(), r.getRight()))
	    .filter(s -> s.player == playerType && s.koma != Koma.Ou)
	    .flatMap(from -> {
		var tryNari = SuperBool.newi();
		return createBranchStream(outeState.y, from)
		    .filter(i -> tryNari.get()
			    || Ban.validateState(from.koma, outeState.x, outeState.y, playerType))
		    .mapToObj(i -> {
			var newBan = ban.clone();
			var newState = newBan.advance(
			    from.x, from.y, outeState.x, outeState.y, tryNari.getAndReverse());
			return checkingSafe(newBan)
				? context.branch(newBan, newState, outeState.koma, playerType, true)
				: null;
		    });
	    });

	// 持ち駒を貼る
	var outeVector = opponentOu.getVectorTo(outeState);
	boolean outeIsNotLinear = Math.abs(outeVector.x) == 1 || Math.abs(outeVector.y) == 1;
	Stream<BanContext> cs3 = context.opponentMotigoma.isEmpty() || outeIsNotLinear
		? Stream.empty()
		: Arrays.stream(MatrixResolver.decompose(outeVector))
		    .filter(miniV -> !outeVector.equals(miniV))
		    .flatMap(v -> context.opponentMotigoma
			.stream()
			.distinct()
			.map(k -> {
			    var newBan = ban.clone();
			    int x = opponentOu.x + v.x, y = opponentOu.y + v.y;
			    var s = newBan.deploy(k, x, y, playerType);
			    return s == MasuState.Invalid
				    ? null
				    : context.branch(newBan, s, k, playerType, false);
			}));

	return executeCS(context, Stream.concat(Stream.concat(cs1, cs2), cs3));
    }

    private BanContext[] executeCS(BanContext beforeContext, Stream<BanContext> cs) {
	if (this.numOfMoves <= beforeContext.log.size()) {
	    return cs.anyMatch(c -> c != null) ? dummyResult : null;
	} else
	    return cs.filter(c -> c != null).toArray(BanContext[]::new);
    }

    private static boolean checkingSafe(Ban ban) {
	return checkingSafe(ban, ban.search(MasuState::isOpponentOu).findFirst().get());
    }

    private static boolean checkingSafe(Ban ban, MasuState state) {
	return state.rangedBy
	    .stream()
	    .map(r -> ban.getState(r.getLeft(), r.getRight()))
	    .allMatch(s -> s.player == playerType);
    }

    @Override
    protected Player getPlayerType() {
	return playerType;
    }
}
