package begyyal.shogi.processor;

import java.util.Comparator;

import begyyal.commons.object.Tree;
import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.object.MasuState;
import begyyal.shogi.object.ResultRecord;

public class ReverseDerivationCalculator {

    public ReverseDerivationCalculator() {
    }

    public XList<MasuState> calculateConclusion(Tree<ResultRecord> tree) {
	var resultTree = r4selectConclusion(tree, true);
	if (resultTree == null)
	    return null;
	return resultTree.traceRoots().stream()
	    .map(t -> t.getValue().state)
	    .filter(s -> s != null)
	    .collect(XListGen.collect());
    }

    // return -> 選択結果の末端ツリーノード。無ければnull。
    @SuppressWarnings("unchecked")
    private Tree<ResultRecord> r4selectConclusion(Tree<ResultRecord> tree, boolean isSelf) {
	// 自分は選択の余地があり、相手の選択は全てカバーしている必要がある
	// つまり、自分はorかつ相手はandで詰みを再帰的に判断する

	var children = tree.getChildren();
	if (children.isEmpty())
	    return tree.getDepth() % 2 == 1 ? tree : null;

	Tree<ResultRecord> result = null;
	long criterion = 0, criterion2 = 0, temp;

	for (Tree<ResultRecord> child : children.stream()
	    .sorted((a, b) -> a.getValue().id - b.getValue().id)
	    .toArray(Tree[]::new)) {

	    var selected = r4selectConclusion(child, !isSelf);

	    if (isSelf) {
		if (selected != null && (result == null || result.getDepth() > selected.getDepth()))
		    result = selected;
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
}
