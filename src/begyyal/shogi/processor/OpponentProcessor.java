package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.Stream;

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

	var ban = context.ban;
	var ou = ban.search(MasuState::isOpponentOu).findFirst().get();

	// 王手範囲から避ける(王による王手駒の取得含む)
	Stream<BanContext> cs1 = spreadMasuState(ou, ban)
	    .filter(s -> ban.checkingSafe(s))
	    .map(s -> {
		var newBan = ban.clone();
		var newState = newBan.advance(ou.x, ou.y, s.x, s.y, false);
		return newBan.checkingSafe(newState)
			? context.branch(newBan, newState, s.koma, playerType, true)
			: null;
	    });

	var outeArray = ou.rangedBy.stream()
	    .map(r -> ban.getState(r.v1, r.v2))
	    .filter(s -> s.player != playerType)
	    .toArray(MasuState[]::new);
	if (outeArray.length > 1)
	    return executeCS(context, cs1);

	// 王手駒を取得する(王による取得は含まず)
	var outeState = outeArray[0];
	Stream<BanContext> cs2 = outeState.rangedBy.stream()
	    .map(r -> ban.getState(r.v1, r.v2))
	    .filter(s -> s.player == playerType && s.koma != Koma.Ou)
	    .flatMap(from -> createBranchStream(outeState.y, from)
		.map(tryNari -> {
		    var newBan = ban.clone();
		    var newState = newBan.advance(
			from.x, from.y, outeState.x, outeState.y, tryNari);
		    return newState != MasuState.Invalid && newBan.checkingSafe()
			    ? context.branch(newBan, newState, outeState.koma, playerType, true)
			    : null;
		}));

	// 合駒(持ち駒を貼る+駒を移動する)
	var outeVector = ou.getVectorTo(outeState);
	Stream<BanContext> cs3 = Math.abs(outeVector.x) == 1 || Math.abs(outeVector.y) == 1
		? Stream.empty()
		: Arrays.stream(outeVector.decompose())
		    .filter(v -> !outeVector.equals(v) &&
			    ban.getState(ou.x + v.x, ou.y + v.y).rangedBy.stream()
				.map(p -> ban.getState(p.v1, p.v2))
				.anyMatch(s -> s.player == playerType && s.koma != Koma.Ou))
		    .flatMap(v -> getOuteObstructionCS(ou.x + v.x, ou.y + v.y, context, ban));

	return executeCS(context, Stream.concat(Stream.concat(cs1, cs2), cs3));
    }

    private Stream<BanContext> getOuteObstructionCS(int x, int y, BanContext context, Ban ban) {

	var state = ban.getState(x, y);
	var c1 = state.rangedBy.stream()
	    .map(p -> ban.getState(p.v1, p.v2))
	    .filter(s -> s.player == playerType && s.koma != Koma.Ou)
	    .flatMap(from -> createBranchStream(state.y, from)
		.map(tryNari -> {
		    var newBan = ban.clone();
		    var newState = newBan.advance(from.x, from.y, x, y, tryNari);
		    return newState == MasuState.Invalid ? null
			    : context.branch(newBan, newState, state.koma, playerType, true);
		}));

	if (context.opponentMotigoma.isEmpty())
	    return c1;

	var c2 = context.opponentMotigoma.stream()
	    .distinct()
	    .map(k -> {
		var newBan = ban.clone();
		var newState = newBan.deploy(k, x, y, playerType);
		return newState == MasuState.Invalid ? null
			: context.branch(newBan, newState, k, playerType, false);
	    });

	return Stream.concat(c1, c2);
    }

    private BanContext[] executeCS(BanContext beforeContext, Stream<BanContext> cs) {
	if (this.numOfMoves <= beforeContext.log.size()) {
	    return cs.anyMatch(c -> c != null) ? dummyResult : null;
	} else
	    return cs.filter(c -> c != null).toArray(BanContext[]::new);
    }

    @Override
    protected Player getPlayerType() {
	return playerType;
    }
}
