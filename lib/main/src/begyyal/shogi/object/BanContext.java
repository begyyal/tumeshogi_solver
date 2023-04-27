package begyyal.shogi.object;

import java.util.concurrent.atomic.AtomicInteger;

import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.object.cache.ContextCache;
import begyyal.shogi.object.cache.ContextCacheKey;

public class BanContext implements Comparable<BanContext> {

    private static final AtomicInteger idGen = new AtomicInteger();

    public final int id = idGen.getAndIncrement();
    public final XList<KihuRecord> log;
    public final MotigomaState[] motigoma;
    public final Ban ban;

    public BanContext(Ban initBan, MotigomaState[] motigoma) {
	this(XListGen.newi(), initBan, motigoma);
    }

    public BanContext(
	XList<KihuRecord> log,
	Ban ban,
	MotigomaState[] motigoma) {
	this.log = log;
	this.ban = ban;
	this.motigoma = motigoma;
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
	return this.motigoma[player.ordinal() * 7 + koma.midx];
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
	    newMotigoma);
    }

    public ContextCacheKey generateCasheKey() {
	// koma*7,player*2,motigoma*2
	var key = new Object[11];
	this.ban.fillCacheKey(key);
	long m = 1;
	int ki = 9;
	for (int i = 1; i <= 14; i++) {
	    var s = this.motigoma[i - 1];
	    m = m * (s.koma.numLimit + 1) + s.num;
	    if (i % 7 == 0) {
		key[ki++] = m;
		m = 1;
	    }
	}
	return new ContextCacheKey(key);
    }

    public ContextCache createCache(int offset, int depth) {
	return new ContextCache(
	    this.log.createPartialList(offset, this.log.size()),
	    this.motigoma,
	    this.ban,
	    depth);
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
