package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class SenteProcessor extends PlayerProcessorBase {

    public static final Player playerType = Player.Sente;

    public SenteProcessor(int numOfMoves) {
	super(numOfMoves);
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.ban;
	var gyoku = ban.search(MasuState::isGyoku).findFirst().get();

	// 駒の移動による王手(開き王手は除く)
	var cs1 = ban.search(s -> s.ss.player == playerType)
	    .flatMap(from -> spreadMasuState(from, ban)
		.filter(to -> this.getTerritoryAfterMoved(to.ss.y, from.ss)
		    .contains(to.getVectorTo(gyoku.ss)))
		.flatMap(to -> createBranchStream(to.ss.y, from.ss)
		    .map(tn -> {
			var newBan = ban.clone();
			var te = newBan.advance(from.ss.x, from.ss.y, to.ss.x, to.ss.y, tn);
			return te != null && newBan.checkOute()
				? context.branch(newBan, te, to.ss.koma, playerType, true)
				: null;
		    })));

	// 開き王手
	var cs2 = ban.search(s -> s.ss.player == playerType && s.ss.koma.isLinearRange())
	    .map(s -> {
		var v = s.getVectorTo(gyoku.ss);
		if (Math.abs(v.x) <= 1 && Math.abs(v.y) <= 1
			|| !s.getDecomposedTerritory().contains(v))
		    return null;
		var dv = v.decompose();
		MasuState obstruction = null;
		for (var miniV : dv) {
		    var result = ban.exploration(s.ss, miniV);
		    if (result == null)
			break;
		    if (result.ss.koma == Koma.Empty)
			continue;
		    if (obstruction == null) {
			if (result.ss.player == playerType) {
			    obstruction = result;
			} else
			    break;
		    } else if (result.isGyoku()) {
			return obstruction;
		    } else
			break;
		}
		return null;
	    })
	    .filter(s -> s != null)
	    .flatMap(obs -> spreadMasuState(obs, ban)
		.flatMap(to -> createBranchStream(to.ss.y, obs.ss)
		    .map(tn -> {
			var newBan = ban.clone();
			var te = newBan.advance(obs.ss.x, obs.ss.y, to.ss.x, to.ss.y, tn);
			return te != null && newBan.checkOute()
				? context.branch(newBan, te, to.ss.koma, playerType, true)
				: null;
		    })));

	// 持ち駒配置による王手
	var cs3 = Arrays.stream(context.motigoma)
	    .filter(m -> m.player == playerType && m.num > 0)
	    .flatMap(m -> {
		var dt = MasuState.getDecomposedTerritory(m.koma, playerType);
		return ban.search(
		    s -> s.ss.koma == Koma.Empty && dt.contains(s.getVectorTo(gyoku.ss)))
		    .sorted( // 無駄合いの余地を極力無くすため、玉に近い手の優先度を上げる
			(s1, s2) -> s1.getVectorTo(gyoku.ss).compareTo(s2.getVectorTo(gyoku.ss)))
		    .map(to -> {
			var newBan = ban.clone();
			var te = newBan.deploy(m.koma, to.ss.x, to.ss.y, playerType);
			return te != null && newBan.checkOute()
				? context.branch(newBan, te, m.koma, playerType, false)
				: null;
		    });
	    });

	// 駒移動系は空き王手と他で重複し得るのでdistinctする
	var cs4 = Stream.concat(cs1, cs2)
	    .filter(c -> c != null)
	    .collect(Collectors.toMap(c -> c.getLatestRecord(), c -> c, (c1, c2) -> c1))
	    .values()
	    .stream();
	return Stream.concat(cs4, cs3).filter(c -> c != null).toArray(BanContext[]::new);
    }

    @Override
    protected Player getPlayerType() {
	return playerType;
    }
}
