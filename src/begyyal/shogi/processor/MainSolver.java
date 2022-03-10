package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import begyyal.shogi.object.Args;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class MainSolver implements Closeable {

    private final DerivationCalculator dc;

    public MainSolver(Args args) {
	this.dc = new DerivationCalculator(
	    args.numOfMoves,
	    args.initBan,
	    args.selfMotigoma,
	    args.opponentMotigoma);
    }

    public String[] calculate() throws InterruptedException, ExecutionException {
	var result = this.dc.ignite();
	return result == null
		? createFailureLabel()
		: this.summarize(result);
    }

    private String[] createFailureLabel() {
	return new String[] { "Can't solve." };
    }

    private String[] summarize(BanContext result) {
	return result.log.stream().map(s -> writeItte(s)).toArray(String[]::new);
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
