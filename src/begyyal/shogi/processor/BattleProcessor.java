package begyyal.shogi.processor;

import java.util.ArrayList;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class BattleProcessor {

    private final SuperList<BanContext> contexts;

    private BattleProcessor(String motigomaStr, String[] banStrs) {
	this.contexts = SuperListGen.of(BanContext.newi(banStrs, motigomaStr));
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

	var branches = SelfProcessor.newi().spread(acon);
	if (branches != null)
	    this.contexts.addAll(branches);
    }

    private BanContext processOpponent(BanContext acon) {

	this.contexts.removeIf(c -> c.id == acon.id);

	var branches = OpponentProcessor.newi().spread(acon);
	if (branches == null)
	    return acon;
	
	this.contexts.addAll(branches);
	return null;
    }

    private String[] summarize(Ban[] bans) {

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
		.filter(p -> !p.getLeft().equals(p.getRight()))
		.map(p -> p.getRight())
		.filter(s -> s != null)
		.findFirst()
		.get();
    }

    private String writeItte(MasuState state) {
	var sb = new StringBuilder();
	sb.append(state.player());
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
