package begyyal.shogi.processor;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class SelfProcessor extends PlayerProcessorBase {

    public static final Player playerType = Player.Self;
    private final Ban initBan;

    public SelfProcessor(int numOfMoves, Ban initBan) {
	super(numOfMoves);
	this.initBan = initBan;
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.getLatestBan() == null ? this.initBan : context.getLatestBan();
	var opponentOu = ban.search(MasuState::isOpponentOu).findFirst().get();

	// 駒の移動による王手(開き王手は除く)
	var cs1 = ban.search(s -> s.player == playerType)
	    .flatMap(from -> {
		var dt = Sets.newHashSet(from.getDecomposedTerritory());
		if (!from.nariFlag)
		    dt.addAll(MasuState.getDecomposedTerritory(from.koma, true, playerType));
		return spreadMasuState(from, ban)
		    .filter(to -> dt.contains(to.getVectorTo(opponentOu)))
		    .flatMap(to -> createBranchStream(to.y, from)
			.map(tryNari -> {
			    var newBan = ban.clone();
			    var newState = newBan.advance(from.x, from.y, to.x, to.y, tryNari);
			    return newState != MasuState.Invalid && !newBan.checkingSafe()
				    ? context.branch(newBan, newState, to.koma, playerType, true)
				    : null;
			}));
	    });

	// 開き王手
	var cs2 = ban.search(s -> s.player == playerType && MasuState.isLinearRange(s))
	    .map(s -> {
		var v = s.getVectorTo(opponentOu);
		if (Math.abs(v.x) <= 1 && Math.abs(v.y) <= 1
			|| !s.getDecomposedTerritory().contains(v))
		    return null;
		var dv = v.decompose();
		MasuState obstruction = null;
		for (var miniV : dv) {
		    var result = ban.exploration(s, miniV);
		    if (result == MasuState.Invalid)
			break;
		    if (result.koma != Koma.Empty)
			if (obstruction == null) {
			    if (result.player == playerType) {
				obstruction = result;
			    } else
				break;
			} else if (result.isOpponentOu()) {
			    return obstruction;
			} else
			    break;
		}
		return null;
	    })
	    .filter(s -> s != null)
	    .flatMap(obs -> spreadMasuState(obs, ban)
		.flatMap(to -> createBranchStream(to.y, obs)
		    .map(tryNari -> {
			var newBan = ban.clone();
			var newState = newBan.advance(obs.x, obs.y, to.x, to.y, tryNari);
			return newState != MasuState.Invalid && !newBan.checkingSafe()
				? context.branch(newBan, newState, to.koma, playerType, true)
				: null;
		    })));

	// 持ち駒配置による王手
	var cs3 = context.selfMotigoma
	    .stream()
	    .distinct()
	    .flatMap(k -> {
		var dt = MasuState.getDecomposedTerritory(k, false, playerType);
		return ban
		    .search(s -> s.koma == Koma.Empty && dt.contains(s.getVectorTo(opponentOu)))
		    .map(to -> {
			var newBan = ban.clone();
			var newState = newBan.deploy(k, to.x, to.y, playerType);
			return newState != MasuState.Invalid && !newBan.checkingSafe()
				? context.branch(newBan, newState, k, playerType, false)
				: null;
		    });
	    });

	// 駒移動系は空き王手と他で重複し得るのでdistinctする
	var cs4 = Stream.concat(cs1, cs2)
	    .filter(c -> c != null)
	    .collect(Collectors.toMap(c -> c.latestState, c -> c, (c1, c2) -> c1))
	    .values()
	    .stream();
	return Stream.concat(cs4, cs3).filter(c -> c != null).toArray(BanContext[]::new);
    }

    @Override
    protected Player getPlayerType() {
	return playerType;
    }
}
