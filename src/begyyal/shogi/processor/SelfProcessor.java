package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class SelfProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Self;

    private SelfProcessor() {
	super();
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.getLatestBan();

	var contextList = ban.search(s -> s.player() == PlayerType)
	    .flatMap(s -> spreadMasuState(s, ban)
		.filter(range -> ban.validateState(range))
		.filter(range -> spreadMasuState(range, ban)
		    .anyMatch(range2 -> range2.koma() == Koma.Ou && range2.player() != PlayerType))
		.map(range -> Pair.of(s, range)))
	    .map(s -> {
		var newBan = ban.clone();
		var k = newBan.advance(s.getLeft(), s.getRight(), PlayerType);
		return context.branch(newBan, k, PlayerType, true);
	    })
	    .collect(SuperListGen.collect());

	context.selfMotigoma
	    .stream()
	    .flatMap(k -> null);

	return null;
    }

    private Stream<MasuState> spreadMasuState(MasuState state, Ban ban) {
	return state.getTerritory()
	    .stream()
	    .flatMap(v -> spreadOneVector(ban, v, state));
    }

    private Stream<MasuState> spreadOneVector(
	Ban ban,
	Vector v,
	MasuState s) {

	if (Math.abs(v.x()) == 1 || Math.abs(v.y()) == 1) {
	    var result = ban.exploration(s, v);
	    return canAdvanceTo(result) ? Stream.of(result) : Stream.empty();
	}

	var stateBucket = new MasuState[8];
	int i = 0;
	for (var miniV : MatrixResolver.decompose(v)) {
	    var result = ban.exploration(s, miniV);
	    if (canAdvanceTo(result)) {
		stateBucket[i] = result;
		i++;
	    }
	    if (result == MasuState.Invalid || result.koma() != Koma.Empty)
		break;
	}

	return i == 0 ? Stream.empty() : Arrays.stream(stateBucket, 0, i);
    }

    private boolean canAdvanceTo(MasuState state) {
	return state != MasuState.Invalid
		&& (state.koma() == Koma.Empty || state.player() != PlayerType);
    }

    public static SelfProcessor newi() {
	return new SelfProcessor();
    }
}
