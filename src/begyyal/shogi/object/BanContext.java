package begyyal.shogi.object;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Sets;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class BanContext {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    public final SuperList<ResultRecord> log;
    public final SuperList<Koma> selfMotigoma;
    public final SuperList<Koma> opponentMotigoma;

    public Ban ban;
    public final int beforeId;

    public BanContext(
	SuperList<Koma> selfMotigoma,
	SuperList<Koma> opponentMotigoma) {

	this.log = SuperListGen.newi();
	this.selfMotigoma = selfMotigoma;
	this.opponentMotigoma = opponentMotigoma;
	this.beforeId = -1;
    }

    private BanContext(
	SuperList<ResultRecord> log,
	Ban ban,
	SuperList<Koma> selfMotigoma,
	SuperList<Koma> opponentMotigoma,
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
	    SuperListGen.of(this.log),
	    ban,
	    SuperListGen.of(this.selfMotigoma),
	    SuperListGen.of(this.opponentMotigoma),
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
	    results.computeIfAbsent(parentId, k -> Sets.newHashSet()).add(point);
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
