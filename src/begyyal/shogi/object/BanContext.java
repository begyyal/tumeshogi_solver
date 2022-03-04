package begyyal.shogi.object;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import begyyal.commons.object.collection.XGen;
import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class BanContext {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    public final XList<ResultRecord> log;
    public final XList<Koma> selfMotigoma;
    public final XList<Koma> opponentMotigoma;

    public Ban ban;
    public final int beforeId;

    public BanContext(
	XList<Koma> selfMotigoma,
	XList<Koma> opponentMotigoma) {

	this.log = XListGen.newi();
	this.selfMotigoma = selfMotigoma;
	this.opponentMotigoma = opponentMotigoma;
	this.beforeId = -1;
    }

    private BanContext(
	XList<ResultRecord> log,
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
	newContext.log.add(new ResultRecord(latestBan.id, latestState));
	var motigoma = player == Player.Self ? newContext.selfMotigoma
		: newContext.opponentMotigoma;

	if (koma != null && koma != Koma.Empty)
	    if (isAddition)
		motigoma.add(koma);
	    else
		motigoma.remove(koma);

	return newContext;
    }

    public BanContext copyOf(Ban ban) {
	return new BanContext(
	    XListGen.of(this.log),
	    ban,
	    XListGen.of(this.selfMotigoma),
	    XListGen.of(this.opponentMotigoma),
	    this.id);
    }

    public MasuState getLatestState() {
	return this.log.isEmpty() ? null : this.log.getTip().state;
    }

    public void fillResult(Map<Integer, Set<ResultRecord>> results, boolean addDummy) {

	if (this.log.isEmpty())
	    return;

	int parentId = 0;
	for (var point : this.log) {
	    results.computeIfAbsent(parentId, k -> XGen.newHashSet()).add(point);
	    parentId = point.id;
	}

	if (addDummy)
	    results.put(parentId,
		Collections.singleton(new ResultRecord(Ban.generateId(), getLatestState())));
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof BanContext))
	    return false;
	var casted = (BanContext) o;
	return casted.beforeId == this.beforeId &&
		(casted.log.size() == 0 && this.log.size() == 0
			|| casted.getLatestState() == this.getLatestState());
    }

    @Override
    public int hashCode() {
	return id;
    }
}
