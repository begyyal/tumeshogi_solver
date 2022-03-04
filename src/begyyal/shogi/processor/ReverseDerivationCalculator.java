package begyyal.shogi.processor;

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

    @SuppressWarnings("unchecked")
    private Tree<ResultRecord> r4selectConclusion(Tree<ResultRecord> tree, boolean isSelf) {

	var children = tree.getChildren();
	if (children.isEmpty())
	    return tree.getDepth() % 2 == 1 ? tree : null;

	Tree<ResultRecord> result = null;
	int depth = 0;

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
		if (depth < (depth = selected.getDepth()))
		    result = selected;
	    }
	}

	return result;
    }
}
