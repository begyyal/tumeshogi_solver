package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import begyyal.commons.util.object.SuperList;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;

public class DerivationCalculator implements Closeable {

    private final int numOfMoves;
    private final BanContext origin;
    private final CalculationTools tools;

    public DerivationCalculator(
	int numOfMoves,
	Ban initBan,
	SuperList<Koma> selfMotigoma,
	SuperList<Koma> opponentMotigoma) {

	this.numOfMoves = numOfMoves;
	this.origin = new BanContext(selfMotigoma, opponentMotigoma);
	this.tools = new CalculationTools(numOfMoves, initBan);
    }

    public boolean ignite(Set<BanContext> results)
	throws InterruptedException, ExecutionException {
	return processSelf(origin, results, 1);
    }

    private boolean processSelf(BanContext acon, Set<BanContext> results, int count) {

	var branches = this.tools.selfProcessor.spread(acon);
	if (branches.length == 0) {
	    results.add(acon);
	    return true;
	} else
	    return this.spread(branches, results, count);
    }

    private boolean processOpponent(BanContext acon, Set<BanContext> results, int count) {

	var branches = this.tools.opponentProcessor.spread(acon);
	if (branches == null || branches.length == 0) {
	    results.add(acon);
	    return true;
	} else if (count == numOfMoves + 1) {
	    acon.isFailure = true;
	    results.add(acon);
	    return true;
	}

	if (Arrays.stream(branches).anyMatch(b -> {
	    long selfBanCount = b.ban.search(s -> s.player == Player.Self).count();
	    return selfBanCount == 0 || selfBanCount + b.selfMotigoma.size() <= 1;
	})) {
	    acon.isFailure = true;
	    results.add(acon);
	    return true;
	}

	return this.spread(branches, results, count);
    }

    private boolean spread(BanContext[] branches, Set<BanContext> results, int count) {
	if (count <= numOfMoves)
	    for (var b : branches)
		if (count % 2 == 0)
		    this.processSelf(b, results, count + 1);
		else
		    this.processOpponent(b, results, count + 1);
	return true;
    }

    @Override
    public void close() throws IOException {
	this.tools.exe.shutdown();
    }

    private class CalculationTools {
	private final SelfProcessor selfProcessor;
	private final OpponentProcessor opponentProcessor;
	private final ExecutorService exe;

	CalculationTools(int numOfMoves, Ban initBan) {
	    this.selfProcessor = new SelfProcessor(numOfMoves, initBan);
	    this.opponentProcessor = new OpponentProcessor(numOfMoves);
	    this.exe = Executors.newCachedThreadPool();
	}
    }
}
