package begyyal.shogi.object;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
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

    private BanContext(String banStr, String motigomaStr) {
	this.log = SuperListGen.of(Ban.of(banStr));
	this.selfMotigoma = parseMotigoma(Player.Self, motigomaStr);
	this.opponentMotigoma = parseMotigoma(Player.Opponent, motigomaStr);
	this.beforeId = -1;
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

    public Ban getLatestBan() {
	return this.log.getTip();
    }

    public void addLatestBan(Ban ban) {
	this.log.add(ban);
    }

    public BanContext branch(
	Ban latestBan,
	MasuState latestState,
	MasuState beforeLatestState,
	Koma koma,
	Player player,
	boolean isAddition) {

	var newContext = this.copy();
	newContext.log.add(latestBan);
	var motigoma = player == Player.Self ? newContext.selfMotigoma
		: newContext.opponentMotigoma;

	if (koma != null)
	    if (isAddition)
		motigoma.add(koma);
	    else
		motigoma.remove(koma);

	newContext.latestState = latestState;
	newContext.beforeLatestState = beforeLatestState;
	return newContext;
    }

    public BanContext copy() {
	return new BanContext(
	    SuperListGen.of(this.log),
	    SuperListGen.of(this.selfMotigoma),
	    SuperListGen.of(this.opponentMotigoma),
	    this.id);
    }

    private static SuperList<Koma> parseMotigoma(Player player, String arg) {

	var motigoma = SuperListGen.<Koma>newi();
	int xIndex = arg.indexOf(Player.Self.id);
	int yIndex = arg.indexOf(Player.Opponent.id);

	Function<Integer, String> lowerGetter = idx -> arg.substring(1, idx);
	Function<Integer, String> higherGetter = idx -> arg.substring(idx + 1, arg.length());
	String argv = xIndex < yIndex
		? (player == Player.Self ? lowerGetter.apply(yIndex) : higherGetter.apply(yIndex))
		: (player == Player.Self ? higherGetter.apply(xIndex) : lowerGetter.apply(xIndex));

	int i = 0;
	while (i < argv.length()) {

	    var type = Koma.of(argv.substring(i, i + 1));
	    var count = argv.substring(i + 1, i + 2);
	    if (i + 3 < argv.length()) { // 歩は保持数2桁があり得る
		var count2dig = argv.substring(i + 1, i + 3);
		if (StringUtils.isNumeric(count2dig) && ++i > 0)
		    count = count2dig;
	    }

	    IntStream.range(0, Integer.valueOf(count)).forEach(idx -> motigoma.add(type));
	    i += 2;
	}

	return motigoma;
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

    public static BanContext newi(String banStr, String motigomaStr) {
	return new BanContext(banStr, motigomaStr);
    }
}
