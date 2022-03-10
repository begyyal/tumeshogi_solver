package begyyal.shogi.object;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class BanContext {

    private static final AtomicInteger idGen = new AtomicInteger();
    public static final BanContext dummy = new BanContext(XListGen.empty(), XListGen.empty());

    public final int id = idGen.getAndIncrement();
    public final XList<MasuState> log;
    public final XList<Koma> selfMotigoma;
    public final XList<Koma> opponentMotigoma;
    public final Ban ban;
    public final int beforeId;

    public int cacheHash;

    public BanContext(
	XList<Koma> selfMotigoma,
	XList<Koma> opponentMotigoma) {
	this(XListGen.newi(), null, selfMotigoma, opponentMotigoma, -1);
    }

    private BanContext(
	XList<MasuState> log,
	Ban ban,
	XList<Koma> selfMotigoma,
	XList<Koma> opponentMotigoma,
	int beforeId) {

	this.log = log;
	this.ban = ban;
	this.selfMotigoma = selfMotigoma;
	this.opponentMotigoma = opponentMotigoma;
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
	var motigoma = player == Player.Self ? newContext.selfMotigoma
		: newContext.opponentMotigoma;

	if (koma != null && koma != Koma.Empty)
	    if (isAddition)
		motigoma.add(koma);
	    else
		motigoma.remove(koma);

	newContext.generateCacheHash();
	return newContext;
    }

    public void generateCacheHash() {
	var mhash = Stream.concat(
	    selfMotigoma.stream().map(k -> k.ordinal()),
	    opponentMotigoma.stream().map(k -> k.ordinal() + Koma.values().length))
	    .collect(Collectors.toList());
	this.cacheHash = Objects.hash(log.size(), ban, mhash);
    }

    public BanContext copyOf(Ban ban) {
	return new BanContext(
	    XListGen.of(this.log),
	    ban,
	    XListGen.of(this.selfMotigoma),
	    XListGen.of(this.opponentMotigoma),
	    this.id);
    }

    public BanContext copyWithModifying(XList<MasuState> log) {
	var l = this.log.createPartialList(log.size(), this.log.size());
	l.addAll(0, log);
	return new BanContext(l, ban, selfMotigoma, opponentMotigoma, id);
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
