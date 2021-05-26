package begyyal.shogi.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class BattleProcessor {

    private final SuperList<BanContext> contexts;
    private final int numOfMoves;

    private BattleProcessor(String numStr, String banStr, String motigomaStr) {
	if (!NumberUtils.isParsable(numStr))
	    throw new IllegalArgumentException(
		"The argument of number of moves must be number format.");
	this.numOfMoves = Integer.parseInt(numStr);
	this.contexts = SuperListGen.of(BanContext.newi(banStr, motigomaStr));
    }

    /**
     * 詰将棋専用。
     * 
     * @return 回答
     */
    public String[] calculate() {

	var results = SuperListGen.<BanContext>newi();
	do {
	    for (BanContext acon : this.shallowCopyContexts())
		processSelf(acon);
	    if (this.contexts.isEmpty())
		break;
	    for (BanContext acon : this.shallowCopyContexts())
		processOpponent(acon, results);
	} while (!this.contexts.isEmpty() && this.contexts.getTip().log.size() - 1 < numOfMoves);

	if (results.isEmpty())
	    return new String[] { "詰めませんでした。" };

	var result = results.stream().collect(SuperMapGen.collect(
	    c -> c.log.size(),
	    c -> c,
	    (c1, c2) -> c1.getLatestBan().grading() < c2.getLatestBan().grading() ? c2 : c1))
	    .entrySet()
	    .stream()
	    .sorted((e1, e2) -> e1.getKey() - e2.getKey())
	    .findFirst().get()
	    .getValue();

	return this.summarize(result.log);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<BanContext> shallowCopyContexts() {
	return (ArrayList<BanContext>) this.contexts.clone();
    }

    private void processSelf(BanContext acon) {

	this.contexts.removeIf(c -> c.id == acon.id);

	var branches = SelfProcessor.newi().spread(acon);
	if (branches != null)
	    this.contexts.addAll(branches);
    }

    private void processOpponent(BanContext acon, SuperList<BanContext> results) {

	this.contexts.removeIf(c -> c.id == acon.id);

	var branches = OpponentProcessor.newi().spread(acon);
	if (branches == null || branches.length == 0)
	    results.add(acon);

	Arrays.stream(branches)
	    .filter(c -> {
		long selfBanCount = c.getLatestBan().search(s -> s.player == Player.Self).count();
		return selfBanCount > 0 && selfBanCount + c.selfMotigoma.size() > 1;
	    })
	    .forEach(this.contexts::add);
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

    public static BattleProcessor of(String numStr, String banStr, String motigomaStr) {
	return new BattleProcessor(numStr, banStr, motigomaStr);
    }
}
