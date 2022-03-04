package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import begyyal.commons.object.Tree;
import begyyal.commons.object.collection.XGen;
import begyyal.commons.object.collection.XList;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.ResultRecord;

public class DerivationCalculator implements Closeable {

    private final int numOfMoves;
    private final Ban initBan;
    private final BanContext origin;
    private final CalculationTools tools;

    public DerivationCalculator(
	int numOfMoves,
	Ban initBan,
	XList<Koma> selfMotigoma,
	XList<Koma> opponentMotigoma) {

	this.numOfMoves = numOfMoves;
	this.initBan = initBan;
	this.origin = new BanContext(selfMotigoma, opponentMotigoma);
	this.tools = new CalculationTools(numOfMoves, initBan);
    }

    private void ignite(Map<Integer, Set<ResultRecord>> results)
	throws InterruptedException, ExecutionException {
	var branches = this.tools.selfProcessor.spread(origin);
	if (branches.length != 0)
	    this.spread(branches, results, 1);
    }

    private void postProcessSelf(
	BanContext acon,
	BanContext[] branches,
	Map<Integer, Set<ResultRecord>> results,
	int count) throws InterruptedException, ExecutionException {

	if (branches.length == 0) {
	    acon.fillResult(results, false);
	} else
	    this.spread(branches, results, count);
    }

    private void postProcessOpponent(
	BanContext acon,
	BanContext[] branches,
	Map<Integer, Set<ResultRecord>> results,
	int count) throws InterruptedException, ExecutionException {

	if (branches == null || branches.length == 0) {
	    acon.fillResult(results, false);

	} else if (count == numOfMoves + 1) {
	    acon.fillResult(results, true);

	} else if (Arrays.stream(branches).anyMatch(b -> {
	    long selfBanCount = b.ban.search(s -> s.player == Player.Self).count();
	    return selfBanCount == 0 || selfBanCount + b.selfMotigoma.size() <= 1;
	})) {
	    acon.fillResult(results, true);

	} else
	    this.spread(branches, results, count);
    }

    private void spread(
	BanContext[] branches,
	Map<Integer, Set<ResultRecord>> results,
	int count)
	throws InterruptedException, ExecutionException {

	var futureMap = XGen.<BanContext, Future<BanContext[]>>newHashMap();
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

    public Tree<ResultRecord> calculateDerivationTree()
	throws InterruptedException, ExecutionException {

	var results = XGen.<Integer, Set<ResultRecord>>newHashMap();
	this.ignite(results);
	if (results.isEmpty())
	    return null;

	var tree = Tree.newi(new ResultRecord(this.initBan.id, null), null);
	r4recordmap2tree(results, tree);
	return tree;
    }

    private void r4recordmap2tree(
	Map<Integer, Set<ResultRecord>> results,
	Tree<ResultRecord> tree) {

	var childrenRec = results.get(tree.getValue().id);
	if (childrenRec == null)
	    return;

	for (var r : childrenRec)
	    tree.addChild(r);
	results.remove(tree.getValue().id);

	for (var c : tree.getChildren())
	    r4recordmap2tree(results, c);
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
