package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class MainSolver implements Closeable {

    private final DerivationCalculator dc;
    private final ReverseDerivationCalculator rdc;

    public MainSolver(Args args) {
	validate(args);
	this.dc = new DerivationCalculator(
	    args.numOfMoves,
	    args.initBan,
	    args.selfMotigoma,
	    args.opponentMotigoma);
	this.rdc = new ReverseDerivationCalculator(args.initBan);
    }

    private void validate(Args args) {
	var tooMany = Stream.concat(args.initBan.serializeMatrix().stream().map(s -> s.koma),
	    Stream.concat(args.selfMotigoma.stream(), args.opponentMotigoma.stream()))
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

    private String[] summarize(List<Ban> bans) {

	var tejun = SuperListGen.<MasuState>newi();

	Ban from = null;
	for (Ban to : bans) {
	    if (from != null)
		tejun.add(parseBanDiff(from, to));
	    from = to;
	}

	return tejun.stream()
	    .map(s -> writeItte(s))
	    .toArray(String[]::new);
    }

    private MasuState parseBanDiff(Ban from, Ban to) {
	return from.serializeMatrix()
	    .zip(to.serializeMatrix())
	    .stream()
	    .filter(p -> !p.getLeft().isEqualWithoutRange(p.getRight()))
	    .map(p -> p.getRight())
	    .filter(s -> s.koma != Koma.Empty)
	    .findFirst()
	    .get();
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
