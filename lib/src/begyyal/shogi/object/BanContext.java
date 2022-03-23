package begyyal.shogi.object;

import java.util.concurrent.atomic.AtomicInteger;

import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.common.Player;

public class BanContext implements Comparable<BanContext> {

    private static final AtomicInteger idGen = new AtomicInteger();
    public static final BanContext dummy = new BanContext(null, null);

    public final int id = idGen.getAndIncrement();
    public final XList<KihuRecord> log;
    public final MotigomaState[] motigoma;
    public final Ban ban;
    public final int beforeId;

    public BanContext(Ban initBan, MotigomaState[] motigoma) {
	this(XListGen.newi(), initBan, motigoma, -1);
    }

    private BanContext(
	XList<KihuRecord> log,
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
	KihuRecord kihuRecord,
	Koma koma,
	Player player,
	boolean isAddition) {

	var newContext = this.copyOf(latestBan);
	newContext.log.add(kihuRecord);

	if (koma != null && koma != Koma.Empty)
	    if (isAddition)
		newContext.getMotigomaState(koma, player).num++;
	    else
		newContext.getMotigomaState(koma, player).num--;

	return newContext;
    }

    private MotigomaState getMotigomaState(Koma koma, Player player) {
	return this.motigoma[player.ordinal() * 7 + koma.ordinal()];
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

    public ContextCacheKey generateCasheKey() {

	// koma*7,player*2,motigoma*1,(motigoma+logSize)*1
	var key = new Object[11];
	this.ban.fillCacheKey(key);

	int mf = this.log.size();
	long m = 1;
	for (int i = 0; i < 14; i++) {
	    var s = this.motigoma[i];
	    if (i % 7 == 0)
		mf = mf * 19 + s.num;
	    else
		m = m * (s.koma.numLimit + 1) + s.num;
	}

	key[9] = m;
	key[10] = mf;

	return new ContextCacheKey(key);
    }

    public BanContext copyWithModifying(XList<KihuRecord> log) {
	var l = this.log.createPartialList(log.size(), this.log.size());
	l.addAll(0, log);
	return new BanContext(l, ban, motigoma, id);
    }

    public KihuRecord getLatestRecord() {
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
	return 0;
    }

    @Override
    public int compareTo(BanContext o) {
	// 愚直な実装順、合駒を用いた回答の優先順位を下げる
	return this.id - o.id;
    }
}
