package begyyal.shogi.processor;

import java.util.Comparator;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.Tree;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class ReverseDerivationCalculator {

    private final Ban initBan;

    public ReverseDerivationCalculator(Ban initBan) {
	this.initBan = initBan;
    }

    public SuperList<MasuState> calculate(Set<BanContext> results) {
	var resultTree = recursive4selectContext(context2tree(results), true);
	if (resultTree == null)
	    return null;
	return results
	    .stream()
	    .filter(c -> c.ban.id == resultTree.getValue())
	    .map(c -> c.log.getV2List())
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
    private Tree<Integer> context2tree(Set<BanContext> results) {

	var origin = Tree.newi(this.initBan.id, null);
	results.stream()
	    .map(c -> {
		var idList = c.log.getV1List();
		if (c.isFailure)
		    idList.add(Ban.generateId());
		return idList;
	    })
	    .forEach(idList -> origin.compound(idList));

	return origin;
    }
}
