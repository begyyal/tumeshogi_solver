package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.Stream;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.Ban.AdvanceState;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class SelfProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Self;

    private SelfProcessor() {
	super();
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.getLatestBan();
	var motigoma = context.selfMotigoma;
	var motigomaBucket = new Koma[1];
	var contextBucket = new BanContext[8];

	ban.search(s -> s.player() == PlayerType)
	    .stream()
	    .flatMap(s -> s.getTerritory()
		.stream()
		.flatMap(v -> spreadOneVector(contextBucket, motigomaBucket, context, ban, v, s)))
	    .filter(b -> b != null)
	    .collect(SuperListGen.collect());

	// 持ち駒パターン

	return null;
    }

    private Stream<BanContext> spreadOneVector(
	BanContext[] contextBucket,
	Koma[] motigomaBucket,
	BanContext context,
	Ban ban,
	Vector v,
	MasuState s) {

	Arrays.setAll(contextBucket, i -> null);
	int i = 0;
	for (var miniV : MatrixResolver.decompose(v)) {
	    var newBan = ban.clone();
	    motigomaBucket[0] = null;
	    var result = newBan.advanceItte(s, miniV, PlayerType, motigomaBucket);
	    if (result == AdvanceState.None || result == AdvanceState.KnockDown)
		contextBucket[i] = context.branch(newBan, motigomaBucket[0], PlayerType, true);
	    i++;
	    if (result == AdvanceState.NoPassage || result == AdvanceState.KnockDown)
		break;
	}

	return Arrays.stream(contextBucket, 0, i);
    }

    public static SelfProcessor newi() {
	return new SelfProcessor();
    }
}
