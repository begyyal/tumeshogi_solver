package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import begyyal.shogi.object.Args;

public class MainSolver implements Closeable {

    private final DerivationCalculator dc;
    private final Summarizer srz;

    public MainSolver(Args args) {
	this.dc = new DerivationCalculator(
	    args.numOfMoves,
	    args.initBan,
	    args.motigoma);
	this.srz = new Summarizer(args.translate);
    }

    public String[] calculate() throws InterruptedException, ExecutionException {
	var result = this.dc.ignite();
	return result == null
		? createFailureLabel()
		: this.srz.exe(result);
    }

    private String[] createFailureLabel() {
	return new String[] { "詰めませんでした。" };
    }

    @Override
    public void close() throws IOException {
	this.dc.close();
    }
}
