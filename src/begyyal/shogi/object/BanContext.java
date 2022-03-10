package begyyal.shogi.object;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class BanContext {

    private static final AtomicInteger idGen = new AtomicInteger();
    public static final BanContext dummy = new BanContext(null);

    public final int id = idGen.getAndIncrement();
    public final XList<MasuState> log;
    public final MotigomaState[] motigoma;
    public final Ban ban;
    public final int beforeId;

    public int cacheHash;

    public BanContext(MotigomaState[] motigoma) {
	this(XListGen.newi(), null, motigoma, -1);
    }

    private BanContext(
	XList<MasuState> log,
	Ban ban,
	MotigomaState[] motigoma,
	int beforeId) {

	this.log = log;
	this.ban = ban;
	this.motigoma = motigoma;
	this.beforeId = beforeId;
    }

    public BanContext branch(
	Ban latestBan,
	MasuState latestState,
	Koma koma,
	Player player,
	boolean isAddition) {

	var newContext = this.copyOf(latestBan);
	newContext.log.add(latestState);

	if (koma != null && koma != Koma.Empty)
	    if (isAddition)
		newContext.getMotigomaState(koma, player).num++;
	    else
		newContext.getMotigomaState(koma, player).num--;

	newContext.generateCacheHash();

	return newContext;
    }

    private MotigomaState getMotigomaState(Koma koma, Player player) {
	return this.motigoma[player.ordinal() * 7 + koma.ordinal()];
    }

    public void generateCacheHash() {
	var mhash = Objects.hash((Object[]) motigoma);
	this.cacheHash = Objects.hash(log.size(), ban, mhash);
    }

    public BanContext copyOf(Ban ban) {

	var newMotigoma = new MotigomaState[14];
	for (int i = 0; i < 14; i++) {
	    var ms = this.motigoma[i];
	    newMotigoma[i] = new MotigomaState(ms.koma, ms.player, ms.num);
	}

	return new BanContext(
	    XListGen.of(this.log),
	    ban,
	    newMotigoma,
	    this.id);
    }

    public BanContext copyWithModifying(XList<MasuState> log) {
	var l = this.log.createPartialList(log.size(), this.log.size());
	l.addAll(0, log);
	return new BanContext(l, ban, motigoma, id);
    }

    public MasuState getLatestState() {
	return this.log.isEmpty() ? null : this.log.getTip();
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof BanContext))
	    return false;
	var casted = (BanContext) o;
	return this.id == casted.id;
    }

    @Override
    public int hashCode() {
	// 合駒を用いた回答の優先順位を下げる
	return id;
    }
}
