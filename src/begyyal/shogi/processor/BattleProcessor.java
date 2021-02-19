package begyyal.shogi.processor;

import java.util.ArrayList;
import java.util.function.Consumer;

import begyyal.commons.util.object.PairList;
import begyyal.commons.util.object.PairList.PairListGen;
import begyyal.commons.util.object.SuperBool;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class BattleProcessor {

    private final SelfProcessor self;
    private final OpponentProcessor opponent;
    private final SuperList<BanContext> contexts;

    private BattleProcessor(String motigomaStr, String[] banStrs) {
	this.self = SelfProcessor.newi(motigomaStr);
	this.opponent = OpponentProcessor.newi(motigomaStr);
	this.contexts = SuperListGen.of(BanContext.newi(banStrs));
    }

    /**
     * 詰将棋専用。
     * 
     * @return 回答
     */
    public String[] calculate() {

	BanContext result = null;
	do {
	    for (BanContext acon : this.shallowCopyContexts())
		processSelf(acon);
	    if (this.contexts.isEmpty())
		break;
	    for (BanContext acon : this.shallowCopyContexts())
		if ((result = processOpponent(acon)) != null)
		    break;
	} while (result == null);

	return result == null 
		? new String[] { "詰めませんでした。" } 
		: this.summarize(result.getLog());
    }

    @SuppressWarnings("unchecked")
    private ArrayList<BanContext> shallowCopyContexts() {
	return (ArrayList<BanContext>) this.contexts.clone();
    }

    private void processSelf(BanContext acon) {

	this.contexts.removeIf(c -> c.id == acon.id);

	var candidates = this.self.spread(acon.getLatestBan());
	if (candidates == null)
	    return;

	for (Ban candidate : candidates) {
	    var newCon = acon.copy();
	    newCon.addLatestBan(candidate);
	    this.contexts.add(newCon);
	}
    }

    private BanContext processOpponent(BanContext acon) {

	this.contexts.removeIf(c -> c.id == acon.id);

	var candidates = this.opponent.spread(acon.getLatestBan());
	if (candidates == null)
	    return acon;

	for (Ban candidate : candidates) {
	    var newCon = acon.copy();
	    newCon.addLatestBan(candidate);
	    this.contexts.add(newCon);
	}
	return null;
    }

    private String[] summarize(Ban[] bans) {

	SuperList<MasuState> tejun = SuperListGen.newi();

	Ban from = null;
	for (Ban to : bans) {
	    if (from != null)
		tejun.add(parseBanDiff(from, to));
	    from = to;
	}

	var turnFlg = SuperBool.of(true);
	return tejun.stream()
		.map(s -> writeItte(s, turnFlg.getAndReverse()))
		.toArray(String[]::new);
    }

    private MasuState parseBanDiff(Ban from, Ban to) {
	return from.serializeMatrix()
		.zip(to.serializeMatrix())
		.stream()
		.filter(p -> !p.getLeft().equals(p.getRight()))
		.map(p -> p.getRight())
		.filter(s -> s != null)
		.findFirst()
		.get();
    }

    private String writeItte(MasuState state, boolean isSelf) {
	var sb = new StringBuilder();
	sb.append(isSelf ? Player.Self : Player.Opponent);
	sb.append(" -> ");
	sb.append(state.suzi());
	sb.append(state.dan());
	sb.append(state.koma());
	sb.append(state.nariFlag());
	return sb.toString();
    }
    
    public static BattleProcessor of(String motigomaStr, String[] banStrs) {
	return new BattleProcessor(motigomaStr, banStrs);
    }
}
