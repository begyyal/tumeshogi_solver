package begyyal.shogi.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.Tree;
import begyyal.shogi.Entrypoint.TRLogger;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class BattleProcessor {

    private final SuperList<BanContext> contexts;
    private final int numOfMoves;
    private final SelfProcessor selfProcessor;
    private final OpponentProcessor opponentProcessor;
    
    private BattleProcessor(String numStr, String banStr, String motigomaStr) {
	if (!NumberUtils.isParsable(numStr))
	    throw new IllegalArgumentException(
		"The argument of number of moves must be number format.");
	this.numOfMoves = Integer.parseInt(numStr);
	if (this.numOfMoves % 2 != 1)
	    throw new IllegalArgumentException(
		"The argument of number of moves must be odd number.");
	this.contexts = SuperListGen.of(BanContext.newi(banStr, motigomaStr));
	this.selfProcessor = SelfProcessor.newi();
	this.opponentProcessor = OpponentProcessor.newi();
    }

    /**
     * 詰将棋専用。
     * 
     * @return 回答
     */
    public String[] calculate() {

	var results = SuperListGen.<BanContext>newi();
	int count = 0;

	do {
	    for (BanContext acon : this.shallowCopyContexts())
		processSelf(acon);
	    count++;
	    if (this.contexts.isEmpty())
		break;
	    for (BanContext acon : this.shallowCopyContexts())
		processOpponent(acon, results, count);
	    count++;
	} while (!this.contexts.isEmpty() && count < numOfMoves);

	if (results.isEmpty())
	    return createFailureLabel();

	var result = this.selectContext(results);
	if (result == null)
	    return createFailureLabel();

	return this.summarize(result.log);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<BanContext> shallowCopyContexts() {
	return (ArrayList<BanContext>) this.contexts.clone();
    }

    private String[] createFailureLabel() {
	return new String[] { "詰めませんでした。" };
    }

    private void processSelf(BanContext acon) {

	this.contexts.removeIf(c -> c.id == acon.id);
	
	var branches = this.selfProcessor.spread(acon);
	if (branches != null)
	    this.contexts.addAll(branches);
    }

    private void processOpponent(BanContext acon, SuperList<BanContext> results, int count) {

	this.contexts.removeIf(c -> c.id == acon.id);
	
	var branches = this.opponentProcessor.spread(acon);
	if (branches == null || branches.length == 0) {
	    results.add(acon);
	    return;
	} else if (count == numOfMoves) {
	    acon.isFailure = true;
	    results.add(acon);
	    return;
	}

	if (Arrays.stream(branches).anyMatch(b -> {
	    long selfBanCount = b.getLatestBan().search(s -> s.player == Player.Self).count();
	    return selfBanCount == 0 || selfBanCount + b.selfMotigoma.size() <= 1;
	})) {
	    acon.isFailure = true;
	    results.add(acon);
	    return;
	}

	this.contexts.addAll(branches);
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

    private BanContext selectContext(SuperList<BanContext> results) {
	var resultTree = recursive4selectContext(context2tree(results), true);
	return resultTree == null
		? null
		: results
		    .stream()
		    .filter(c -> c.getLatestBan().id == resultTree.getValue())
		    .findFirst().get();
    }

    // return -> 選択結果の末端ツリーノード。無ければnull。
    private Tree<Integer> recursive4selectContext(Tree<Integer> tree, boolean isSelf) {
	// 自分は選択の余地があり、相手の選択は全てカバーしている必要がある
	// つまり、自分はorかつ相手はandで詰みを再帰的に判断する

	if (CollectionUtils.isEmpty(tree.getChildren()))
	    return tree.getDepth() % 2 == 1 ? tree : null;

	Tree<Integer> result = null;
	long criterion = 0, criterion2 = 0, temp;

	for (var child : tree.getChildren()) {
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
    private Tree<Integer> context2tree(SuperList<BanContext> results) {

	var origin = Tree.newi(results.get(0).log.get(0).id, null);
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

    public static BattleProcessor of(String numStr, String banStr, String motigomaStr) {
	return new BattleProcessor(numStr, banStr, motigomaStr);
    }
}
