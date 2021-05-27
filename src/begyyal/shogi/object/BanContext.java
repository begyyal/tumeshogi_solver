package begyyal.shogi.object;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class BanContext {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    private static final String motigomaArgRegex = "([xy]([a][1-9][1-8]?|[b-e][1-4]|[fg][12])+){2}";

    public final SuperList<Ban> log;
    // 持ち駒は最新断面のみ
    public final SuperList<Koma> selfMotigoma;
    public final SuperList<Koma> opponentMotigoma;

    public MasuState latestState;
    // 低コストなequalsを行うためにlatestStateと併せて3点セットで保持
    public MasuState beforeLatestState;
    public final int beforeId;

    public boolean isFailure = false;

    private BanContext(String banStr, String motigomaStr) {
	var ban = Ban.of(banStr);
	this.log = SuperListGen.of(Ban.of(banStr));
	this.selfMotigoma = parseMotigoma(Player.Self, motigomaStr);
	this.opponentMotigoma = parseMotigoma(Player.Opponent, motigomaStr);
	this.beforeId = -1;
	validateCondition(ban);
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

    // TODO
    public BanContext branch(BranchParam param) {
	return branch(
	    param.latestBan, param.latestState, param.koma, param.player, param.isAddition);
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

	if (koma != null)
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

    private static SuperList<Koma> parseMotigoma(Player player, String arg) {

	var motigoma = SuperListGen.<Koma>newi();
	if (arg == null)
	    return motigoma;

	if (!arg.matches(motigomaArgRegex))
	    throw new IllegalArgumentException("Motigoma argument format is invalid.");

	int xIndex = arg.indexOf(Player.Self.id);
	int yIndex = arg.indexOf(Player.Opponent.id);
	if (player == Player.Self ? yIndex < 0 : xIndex < 0)
	    getMotigomaParser(motigoma).accept(arg);
	if (xIndex < 0 || yIndex < 0)
	    return motigoma;

	Function<Integer, String> lowerGetter = idx -> arg.substring(1, idx);
	Function<Integer, String> higherGetter = idx -> arg.substring(idx + 1, arg.length());
	String argv = xIndex < yIndex
		? (player == Player.Self ? lowerGetter.apply(yIndex) : higherGetter.apply(yIndex))
		: (player == Player.Self ? higherGetter.apply(xIndex) : lowerGetter.apply(xIndex));
	getMotigomaParser(motigoma).accept(argv);

	return motigoma;
    }

    private static Consumer<String> getMotigomaParser(SuperList<Koma> motigoma) {
	return arg -> {
	    int i = 0;
	    while (i < arg.length()) {

		var type = Koma.of(arg.substring(i, i + 1));
		var count = arg.substring(i + 1, i + 2);
		if (i + 3 < arg.length()) { // 歩は保持数2桁があり得る
		    var count2dig = arg.substring(i + 1, i + 3);
		    if (StringUtils.isNumeric(count2dig) && ++i > 0)
			count = count2dig;
		}

		IntStream.range(0, Integer.valueOf(count)).forEach(idx -> motigoma.add(type));
		i += 2;
	    }
	};
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

    // TODO
    @SuppressWarnings("preview")
    public record BranchParam(
	    Ban latestBan,
	    MasuState latestState,
	    Koma koma,
	    Player player,
	    boolean isAddition) {
    }
}
