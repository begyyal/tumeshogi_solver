package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class MainSolver implements Closeable {

    private final DerivationCalculator dc;
    private final ReverseDerivationCalculator rdc;

    public MainSolver(Args args) {

	validate(args.initBan, args.selfMotigoma, args.opponentMotigoma);

	this.dc = new DerivationCalculator(
	    args.numOfMoves,
	    args.initBan,
	    args.selfMotigoma,
	    args.opponentMotigoma);
	this.rdc = new ReverseDerivationCalculator(args.initBan);
    }

    private void validate(Ban ban, SuperList<Koma> selfMotigoma, SuperList<Koma> opponentMotigoma) {

	validate(ban);

	var tooMany = Stream.concat(ban.serializeMatrix().stream().map(s -> s.koma),
	    Stream.concat(selfMotigoma.stream(), opponentMotigoma.stream()))
	    .filter(k -> k != Koma.Empty)
	    .collect(SuperMapGen.collect(k -> k, k -> 1, (v1, v2) -> v1 + v2))
	    .entrySet()
	    .stream()
	    .filter(e -> e.getKey().numLimit < e.getValue())
	    .findFirst()
	    .orElse(null);
	if (tooMany != null)
	    throw new IllegalArgumentException("The koma [" + tooMany + "] exceeeds number limit.");
    }

    private void validate(Ban ban) {

	boolean existOu = false;
	for (int x = 0; x < 9; x++) {
	    int huCountX = 0, huCountY = 0;
	    for (int y = 0; y < 9; y++) {
		var state = ban.getState(x, y);
		if (state.koma == Koma.Empty)
		    continue;
		existOu = existOu || state.koma == Koma.Ou && state.player == Player.Opponent;
		if (state.koma == Koma.Hu && !state.nariFlag
			&& (state.player == Player.Self ? ++huCountX == 2 : ++huCountY == 2))
		    throw new IllegalArgumentException("It's 2hu.");
		if (!state.nariFlag && !Ban.validateState(state.koma, x, y, state.player))
		    throw new IllegalArgumentException("There is invalid arrangement.");
	    }
	}

	if (!existOu)
	    throw new IllegalArgumentException("There must be the koma [Ou] in y's arguments.");
    }

    public String[] calculate()
	throws InterruptedException, ExecutionException {

	var results = Sets.<BanContext>newConcurrentHashSet();

	if (!this.dc.ignite(results) || results.isEmpty())
	    return createFailureLabel();

	var result = this.rdc.calculate(results);
	if (result == null)
	    return createFailureLabel();

	return this.summarize(result);
    }

    private String[] createFailureLabel() {
	return new String[] { "Can't solve." };
    }

    private String[] summarize(SuperList<MasuState> result) {
	return result.stream().map(this::writeItte).toArray(String[]::new);
    }

    private String writeItte(MasuState state) {
	var sb = new StringBuilder();
	sb.append(state.player);
	sb.append(" -> ");
	sb.append(state.getSuzi());
	sb.append(state.getDan());
	sb.append(state.koma);
	if (state.nariFlag)
	    sb.append("Nari");
	return sb.toString();
    }

    @Override
    public void close() throws IOException {
	this.dc.close();
    }
}
