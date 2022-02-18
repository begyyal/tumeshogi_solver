package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Sets;

import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.Tree;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class BattleProcessor implements Closeable {

    private final CalculationTools tools;

    private BattleProcessor() {
	this.tools = new CalculationTools();
    }

    /**
     * 詰将棋専用。
     * 
     * @return 回答
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String[] calculate(String numStr, String banStr, String motigomaStr)
	throws InterruptedException, ExecutionException {

	var results = Sets.<BanContext>newConcurrentHashSet();
	var calculator = new OneshotCalculator(numStr, banStr, motigomaStr, this.tools);

	if (!calculator.ignite(results) || results.isEmpty())
	    return createFailureLabel();

	var result = this.selectContext(calculator.origin, results);
	if (result == null)
	    return createFailureLabel();

	return this.summarize(result.log);
    }

    private String[] createFailureLabel() {
	return new String[] { "詰めませんでした。" };
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

    private BanContext selectContext(BanContext origin, Set<BanContext> results) {
	var resultTree = recursive4selectContext(context2tree(origin, results), true);
	return resultTree == null
		? null
		: results
		    .stream()
		    .filter(c -> c.getLatestBan().id == resultTree.getValue())
		    .findFirst().get();
    }

    // return -> 選択結果の末端ツリーノード。無ければnull。
    @SuppressWarnings("unchecked")
    private Tree<Integer> recursive4selectContext(Tree<Integer> tree, boolean isSelf) {
	// 自分は選択の余地があり、相手の選択は全てカバーしている必要がある
	// つまり、自分はorかつ相手はandで詰みを再帰的に判断する

	if (CollectionUtils.isEmpty(tree.getChildren()))
	    return tree.getDepth() % 2 == 1 ? tree : null;

	Tree<Integer> result = null;
	long criterion = 0, criterion2 = 0, temp;

	for (Tree<Integer> child : tree.getChildren().stream()
	    .sorted((a, b) -> a.getValue() - b.getValue())
	    .toArray(Tree[]::new)) {

	    var selected = recursive4selectContext(child, !isSelf);

	    if (isSelf) {
		if (selected != null)
		    return selected;
	    } else {
		if (selected == null)
		    return null;
		// 相手方は最短詰み筋の深度がより長い選択をする
		var tips = child.collectTips();
		final long minDepth = tips
		    .stream()
		    .map(t -> t.getDepth())
		    .filter(d -> d % 2 == 1)
		    .sorted(Comparator.naturalOrder())
		    .findFirst().get();
		if (criterion == minDepth
			&& criterion2 > (temp = tips
			    .stream()
			    .filter(t -> t.getDepth() == minDepth)
			    .count())
			&& (criterion2 = temp) > 0
			|| criterion < minDepth && (criterion = minDepth) > 0)
		    result = selected;
	    }
	}

	return result;
    }

    // 引数のコンテキストのログを集積して初期配置からの盤面の分岐をツリー化
    private Tree<Integer> context2tree(BanContext originC, Set<BanContext> results) {

	var origin = Tree.newi(originC.log.get(0).id, null);
	results.stream()
	    .map(c -> {
		var idList = c.log.subList(1, c.log.size())
		    .stream()
		    .map(b -> b.id)
		    .collect(Collectors.toList());
		if (c.isFailure)
		    idList.add(Ban.generateId());
		return idList;
	    })
	    .forEach(idList -> origin.compound(idList));

	return origin;
    }

    public static BattleProcessor newi() {
	return new BattleProcessor();
    }

    @Override
    public void close() throws IOException {
	this.tools.exe.shutdown();
    }

    private class CalculationTools {
	private final SelfProcessor selfProcessor;
	private final OpponentProcessor opponentProcessor;
	private final ExecutorService exe;

	CalculationTools() {
	    this.selfProcessor = SelfProcessor.newi();
	    this.opponentProcessor = OpponentProcessor.newi();
	    this.exe = Executors.newCachedThreadPool();
	}
    }

    private class OneshotCalculator {
	private final int numOfMoves;
	private final BanContext origin;
	private final CalculationTools tools;

	private OneshotCalculator(
	    String numStr,
	    String banStr,
	    String motigomaStr,
	    CalculationTools tools) {

	    if (!NumberUtils.isParsable(numStr))
		throw new IllegalArgumentException(
		    "The argument of number of moves must be number format.");
	    this.numOfMoves = Integer.parseInt(numStr);
	    if (numOfMoves % 2 != 1)
		throw new IllegalArgumentException(
		    "The argument of number of moves must be odd number.");
	    this.origin = BanContext.newi(banStr, motigomaStr);
	    this.tools = tools;
	}

	private boolean ignite(Set<BanContext> results)
	    throws InterruptedException, ExecutionException {
	    return this.next(origin, results, 0);
	}

	private boolean next(BanContext acon, Set<BanContext> results, int count)
	    throws InterruptedException, ExecutionException {
	    return count > numOfMoves ||
		    this.tools.exe.submit(
			count % 2 == 0
				? () -> this.processSelf(acon, results, count + 1)
				: () -> this.processOpponent(acon, results, count + 1))
			.get();
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
	    if (branches.length == 0) {
		results.add(acon);
		return true;
	    } else if (count == numOfMoves + 1) {
		acon.isFailure = true;
		results.add(acon);
		return true;
	    }

	    if (Arrays.stream(branches).anyMatch(b -> {
		long selfBanCount = b.getLatestBan().search(s -> s.player == Player.Self).count();
		return selfBanCount == 0 || selfBanCount + b.selfMotigoma.size() <= 1;
	    })) {
		acon.isFailure = true;
		results.add(acon);
		return true;
	    }

	    return this.spread(branches, results, count);
	}

	private boolean spread(BanContext[] branches, Set<BanContext> results, int count) {
	    return Arrays.stream(branches)
		.map(b -> {
		    try {
			return this.next(b, results, count);
		    } catch (InterruptedException | ExecutionException e) {
			return false;
		    }
		})
		.allMatch(b -> b);
	}
    }
}
