package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.Stream;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.def.TryNari;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;
import begyyal.shogi.object.MasuState.SmartMasuState;

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
	    .filter(s -> s.checkSafe(playerType))
	    .map(s -> {
		var newBan = ban.clone();
		var te = newBan.advance(ou.ss.x, ou.ss.y, s.ss.x, s.ss.y, TryNari.Rezu);
		return !newBan.checkOute()
			? context.branch(newBan, te, s.ss.koma, playerType, true)
			: null;
	    });

	var outeArray = ou.rangedBy.stream()
	    .filter(s -> s.player != playerType)
	    .toArray(SmartMasuState[]::new);
	if (outeArray.length > 1)
	    return executeCS(context, cs1);

	// 王手駒を取得する(王による取得は含まず)
	var oute = ban.getState(outeArray[0].x, outeArray[0].y);
	Stream<BanContext> cs2 = oute.rangedBy.stream()
	    .filter(s -> s.player == playerType && s.koma != Koma.Ou)
	    .flatMap(from -> createBranchStream(oute.ss.y, from)
		.map(tn -> {
		    var newBan = ban.clone();
		    var te = newBan.advance(from.x, from.y, oute.ss.x, oute.ss.y, tn);
		    return te != null && !newBan.checkOute()
			    ? context.branch(newBan, te, oute.ss.koma, playerType, true)
			    : null;
		}));

	// 合駒(持ち駒を貼る+駒を移動する)
	var outeVector = ou.getVectorTo(oute);
	Stream<BanContext> cs3 = Math.abs(outeVector.x) == 1 || Math.abs(outeVector.y) == 1
		? Stream.empty()
		: Arrays.stream(outeVector.decompose())
		    .filter(v -> !outeVector.equals(v)
			    && checkMudaai(ban, ban.getState(ou.ss.x + v.x, ou.ss.y + v.y), oute))
		    .flatMap(v -> getOuteObstructionCS(ou.ss.x + v.x, ou.ss.y + v.y, context, ban));

	return executeCS(context, Stream.concat(Stream.concat(cs1, cs2), cs3));
    }

    private static boolean checkMudaai(Ban ban, MasuState state, MasuState outeState) {
	boolean ouYoko = false;
	for (var s : state.rangedBy)
	    if (s.player == playerType && !(ouYoko |= s.koma == Koma.Ou))
		return true;
	return ouYoko && state.rangedBy.stream()
	    .filter(s -> s.hash != outeState.ss.hash)
	    .allMatch(s -> s.player == playerType);
    }

    private Stream<BanContext> getOuteObstructionCS(int x, int y, BanContext context, Ban ban) {

	var state = ban.getState(x, y);
	var c1 = state.rangedBy.stream()
	    .filter(s -> s.player == playerType && s.koma != Koma.Ou)
	    .flatMap(from -> createBranchStream(state.ss.y, from)
		.map(tn -> {
		    var newBan = ban.clone();
		    var te = newBan.advance(from.x, from.y, x, y, tn);
		    return te == null ? null
			    : context.branch(newBan, te, state.ss.koma, playerType, true);
		}));

	if (Arrays.stream(context.motigoma)
	    .filter(m -> m.player == playerType && m.num > 0)
	    .findAny().isEmpty())
	    return c1;

	var c2 = Arrays.stream(context.motigoma)
	    .filter(m -> m.player == playerType && m.num > 0)
	    .map(m -> {
		var newBan = ban.clone();
		var te = newBan.deploy(m.koma, x, y, playerType);
		return te == null ? null
			: context.branch(newBan, te, m.koma, playerType, false);
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
