package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.Stream;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.TryNari;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;
import begyyal.shogi.object.MasuState.SmartMasuState;

public class GoteProcessor extends PlayerProcessorBase {

    public static final Player playerType = Player.Gote;
    private static final BanContext[] dummyResult = new BanContext[] { null };

    public GoteProcessor(int numOfMoves) {
	super(numOfMoves);
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.ban;
	var ou = ban.search(MasuState::isGyoku).findFirst().get();

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

	// 両王手はここで終了
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
	var outeVector = ou.getVectorTo(oute.ss);
	Stream<BanContext> cs3 = !oute.ss.koma.isLinearRange()
		? Stream.empty()
		: Arrays.stream(outeVector.decompose())
		    .filter(v -> !outeVector.equals(v)
			    && checkMudaai(ban, ban.getState(ou.ss.x + v.x, ou.ss.y + v.y), oute))
		    .flatMap(v -> getOuteObstructionCS(ou.ss.x + v.x, ou.ss.y + v.y, context, ban));
	return executeCS(context, Stream.concat(Stream.concat(cs1, cs2), cs3));
    }

    private boolean checkMudaai(Ban ban, MasuState fs, MasuState oute) {

	var grb = fs.rangedBy.stream().filter(s -> s.player == playerType)
	    .toArray(SmartMasuState[]::new);
	if (grb.length == 0)
	    return false;

	var ou = Arrays.stream(grb).filter(s -> s.koma == Koma.Ou)
	    .findFirst().orElse(null);
	if (ou == null)
	    return true;

	var preOute = fs.rangedBy.stream()
	    .filter(s -> s.player != playerType &&
		    this.getTerritoryAfterMoved(fs.ss.y, s, Player.Sente)
			.contains(fs.getVectorTo(ou)))
	    .toArray(SmartMasuState[]::new);
	if (grb.length >= preOute.length)
	    return true;

	var outeSlope = fs.getVectorTo(ou);
	var isPierce = ban.exploration(ou, outeSlope) != null;
	int offset = Math.max(grb.length - 1, 0);
	long nowScore = this.getSmCount(ban, ou) * 10 - (isPierce ? 5 : 0);
	long estScore = Arrays.stream(preOute)
	    .map(ss -> this.createBranchStream(fs.ss.y, ss, Player.Sente).map(tn -> {
		var newBan = ban.clone();
		var te = newBan.advance(ss.x, ss.y, fs.ss.x, fs.ss.y, tn);
		long smc = te != null && newBan.checkOute() ? this.getSmCount(newBan, ou) : 9;
		return smc * 10 - (isPierce && ss.haveLinearWithSameSlope(outeSlope) ? 5 : 0);
	    }).reduce((a, b) -> a < b ? a : b).get())
	    .sorted().skip(offset).findFirst().get();

	return estScore > nowScore;
    }

    private long getSmCount(Ban ban, SmartMasuState ou) {
	return ban.search(s -> {
	    var v = s.getVectorTo(ou);
	    return Math.abs(v.x) <= 1 && Math.abs(v.y) <= 1
		    && s.ss.koma == Koma.Empty
		    && s.rangedBy.stream().allMatch(s2 -> s2.player != Player.Sente);
	}).count();
    }

    private Stream<BanContext> getOuteObstructionCS(int x, int y, BanContext context, Ban ban) {

	var state = ban.getState(x, y);
	var c1 = state.rangedBy.stream()
	    .filter(s -> s.player == playerType && s.koma != Koma.Ou)
	    .flatMap(from -> createBranchStream(state.ss.y, from)
		.map(tn -> {
		    var newBan = ban.clone();
		    var te = newBan.advance(from.x, from.y, x, y, tn);
		    return te == null || newBan.checkOute() ? null
			    : context.branch(newBan, te, state.ss.koma, playerType, true);
		}));

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
