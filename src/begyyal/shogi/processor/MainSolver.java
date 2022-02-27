package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Sets;

import begyyal.commons.util.object.SuperList;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class MainSolver implements Closeable {

    private final DerivationCalculator dc;
    private final ReverseDerivationCalculator rdc;

    public MainSolver(Args args) {
	this.dc = new DerivationCalculator(
	    args.numOfMoves,
	    args.initBan,
	    args.selfMotigoma,
	    args.opponentMotigoma);
	this.rdc = new ReverseDerivationCalculator(args.initBan);
    }

    public String[] calculate() throws InterruptedException, ExecutionException {

	var results = Sets.<BanContext>newHashSet();

	this.dc.ignite(results);
	if (results.isEmpty())
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
