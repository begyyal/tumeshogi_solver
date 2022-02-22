package begyyal.shogi.object;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class BanContext {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    public final SuperList<Ban> log;
    // 持ち駒は最新断面のみ
    public final SuperList<Koma> selfMotigoma;
    public final SuperList<Koma> opponentMotigoma;

    public MasuState latestState;
    // 低コストなequalsを行うためにlatestStateと併せて3点セットで保持
    public MasuState beforeLatestState;
    public final int beforeId;

    // 詰みの失敗は最終的に相手方の手で終わっていることとして判断される
    // 失敗が確定的なselfの打ち筋に対しては、次の相手方の手で無駄なコンテキスト派生をせずに当該フラグを立てる形となる
    // これにより終端のコンテキスト選定の段でフラグを元にダミーの相手方の手をlogに１手だけ加え、失敗を判断する
    public boolean isFailure = false;

    public BanContext(
	Ban initBan, 
	SuperList<Koma> selfMotigoma, 
	SuperList<Koma> opponentMotigoma) {
	
	this.log = SuperListGen.of(initBan);
	this.selfMotigoma = selfMotigoma;
	this.opponentMotigoma = opponentMotigoma;
	this.beforeId = -1;
	
	validateCondition(initBan);
    }

    private BanContext(
	SuperList<Ban> log,
	SuperList<Koma> selfMotigoma,
	SuperList<Koma> opponentMotigoma,
	int beforeId) {
	
	this.log = log;
	this.selfMotigoma = selfMotigoma;
	this.opponentMotigoma = opponentMotigoma;
	this.beforeId = beforeId;
    }

    private void validateCondition(Ban ban) {
	var tooMany = Stream.concat(ban.serializeMatrix().stream().map(s -> s.koma),
	    Stream.concat(this.selfMotigoma.stream(), this.opponentMotigoma.stream()))
	    .filter(k -> k != Koma.Empty)
	    .collect(SuperMapGen.collect(k -> k, k -> 1, (v1, v2) -> v1 + v2))
	    .entrySet()
	    .stream()
	    .filter(e -> e.getKey().numLimit < e.getValue())
	    .findFirst()
	    .orElse(null);
	if (tooMany != null)
	    throw new IllegalArgumentException("The koma [" + tooMany + "] exceeeds number limit.");
    }

    public Ban getBan(int index) {
	return this.log.get(index);
    }

    public Ban getLatestBan() {
	return this.log.getTip();
    }

    public void addLatestBan(Ban ban) {
	this.log.add(ban);
    }

    public BanContext branch(
	Ban latestBan,
	MasuState latestState,
	Koma koma,
	Player player,
	boolean isAddition) {

	var newContext = this.copy();
	newContext.log.add(latestBan);
	var motigoma = player == Player.Self ? newContext.selfMotigoma
		: newContext.opponentMotigoma;

	if (koma != null && koma != Koma.Empty)
	    if (isAddition)
		motigoma.add(koma);
	    else
		motigoma.remove(koma);

	newContext.latestState = latestState;
	newContext.beforeLatestState = this.latestState;
	return newContext;
    }

    public BanContext copy() {
	return new BanContext(
	    SuperListGen.of(this.log),
	    SuperListGen.of(this.selfMotigoma),
	    SuperListGen.of(this.opponentMotigoma),
	    this.id);
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof BanContext))
	    return false;
	var casted = (BanContext) o;
	return casted.beforeId == this.beforeId &&
		casted.latestState == this.latestState &&
		casted.beforeLatestState == this.beforeLatestState;
    }
}
