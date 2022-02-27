package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.Maps;

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

    public void ignite(Set<BanContext> results)
	throws InterruptedException, ExecutionException {
	this.postProcessSelf(origin, this.tools.selfProcessor.spread(origin), results, 1);
    }

    private void postProcessSelf(
	BanContext acon,
	BanContext[] branches,
	Set<BanContext> results,
	int count) throws InterruptedException, ExecutionException {

	if (branches.length == 0) {
	    results.add(acon);
	} else
	    this.spread(branches, results, count);
    }

    private void postProcessOpponent(
	BanContext acon,
	BanContext[] branches,
	Set<BanContext> results,
	int count) throws InterruptedException, ExecutionException {

	if (branches == null || branches.length == 0) {
	    results.add(acon);
	} else if (count == numOfMoves + 1) {
	    acon.isFailure = true;
	    results.add(acon);
	} else if (Arrays.stream(branches).anyMatch(b -> {
	    long selfBanCount = b.ban.search(s -> s.player == Player.Self).count();
	    return selfBanCount == 0 || selfBanCount + b.selfMotigoma.size() <= 1;
	})) {
	    acon.isFailure = true;
	    results.add(acon);
	} else
	    this.spread(branches, results, count);
    }

    private void spread(BanContext[] branches, Set<BanContext> results, int count)
	throws InterruptedException, ExecutionException {

	var futureMap = Maps.<BanContext, Future<BanContext[]>>newHashMap();
	if (count <= numOfMoves)
	    for (var b : branches)
		futureMap.put(b, this.tools.exe.submit(count % 2 == 0
			? () -> this.tools.selfProcessor.spread(b)
			: () -> this.tools.opponentProcessor.spread(b)));

	for (var e : futureMap.entrySet()) {
	    var newBranches = e.getValue().get();
	    if (count % 2 == 0)
		this.postProcessSelf(e.getKey(), newBranches, results, count + 1);
	    else
		this.postProcessOpponent(e.getKey(), newBranches, results, count + 1);
	}
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
