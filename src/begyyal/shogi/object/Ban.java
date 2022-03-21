package begyyal.shogi.object;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.def.TryNari;
import begyyal.shogi.object.MasuState.SmartMasuState;

public class Ban implements Cloneable {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    // インデックスの振り順は将棋盤の読み方に倣わない。x/y座標で見る。
    private MasuState[] matrix;

    public Ban(MasuState[] matrix) {
	this.matrix = matrix;
    }

    public void setup() {
	for (int i = 0; i < 81; i++)
	    if (this.matrix[i] == null)
		this.matrix[i] = MasuState.emptyOf(i / 9, i % 9, XGen.newHashSet());
	for (int i = 0; i < 81; i++)
	    markRangeBy(i);
    }

    public MasuState getState(int x, int y) {
	return this.matrix[x * 9 + y];
    }

    private void refreshRange() {
	for (int i = 0; i < 81; i++)
	    this.matrix[i].rangedBy.clear();
	for (int i = 0; i < 81; i++)
	    markRangeBy(i);
    }

    public void markRangeBy(int i) {
	var s = this.matrix[i];
	if (s.ss.koma == Koma.Empty)
	    return;
	boolean haveLinearRange = MasuState.isLinearRange(s);
	for (var v : s.getTerritory())
	    if (haveLinearRange) {
		for (var miniV : v.decompose())
		    if (!markRange(miniV, s.ss))
			break;
	    } else
		markRange(v, s.ss);
    }

    private boolean markRange(Vector v, SmartMasuState ss) {
	int vx = ss.x + v.x;
	int vy = ss.y + v.y;
	if (!validateCoordinate(vx, vy))
	    return false;
	this.matrix[vx * 9 + vy].rangedBy.add(ss);
	return this.matrix[vx * 9 + vy].ss.koma == Koma.Empty;
    }

    public Stream<MasuState> search(Predicate<MasuState> filter) {
	var result = new MasuState[81];
	int count = 0;
	for (int i = 0; i < 81; i++)
	    if (filter.test(matrix[i]))
		result[count++] = matrix[i];
	return count == 0 ? Stream.empty() : Arrays.stream(result, 0, count);
    }

    public Stream<MasuState> matrixStream() {
	return Arrays.stream(this.matrix);
    }

    public MasuState exploration(MasuState state, Vector v) {
	int x = state.ss.x, y = state.ss.y;
	int vx = x + v.x, vy = y + v.y;
	return validateCoordinate(vx, vy) ? this.matrix[vx * 9 + vy] : MasuState.Invalid;
    }

    public KihuRecord advance(int fromX, int fromY, int toX, int toY, TryNari tn) {

	var from = this.matrix[fromX * 9 + fromY];
	var fss = from.ss;
	var to = this.matrix[toX * 9 + toY];

	boolean naru = tn == TryNari.Ru;
	if (!fss.nari && !naru && !validateState(fss.koma, toX, toY, fss.player))
	    return null;

	var te = KihuRecord.resolveAdvance(to, fss.player, fss.koma, fss.nari, fromX, fromY, tn);

	emptyMasu(fromX, fromY);

	var newState = new MasuState(
	    fss.player,
	    fss.koma,
	    toX,
	    toY,
	    fss.nari || naru,
	    to.rangedBy);
	this.matrix[toX * 9 + toY] = newState;

	refreshRange();

	return te;
    }

    public KihuRecord deploy(Koma k, int x, int y, Player p) {

	if (!validateState(k, x, y, p) || k == Koma.Hu && !checkNihu(p, x))
	    return null;

	var state = this.matrix[x * 9 + y];
	var te = KihuRecord.resolveDeploy(state, p, k);

	var newState = new MasuState(p, k, x, y, false, state.rangedBy);
	this.matrix[x * 9 + y] = newState;

	refreshRange();

	return te;
    }

    public boolean checkNihu(Player player, int x) {
	return search(s -> s.ss.x == x
		&& s.ss.koma == Koma.Hu
		&& !s.ss.nari
		&& s.ss.player == player)
		    .findAny().isEmpty();
    }

    private void emptyMasu(int x, int y) {
	this.matrix[x * 9 + y] = MasuState.emptyOf(x, y, this.matrix[x * 9 + y].rangedBy);
    }

    public boolean checkOute() {
	return !search(MasuState::isOpponentOu).findFirst().get().checkSafe(Player.Opponent);
    }

    public static boolean validateState(Koma koma, int x, int y, Player p) {
	int end = p == Player.Self ? 8 : 0;
	return (koma != Koma.Hu && koma != Koma.Kyousha || y != end)
		&& (koma != Koma.Keima || (p == Player.Self ? y < 7 : y > 1));
    }

    private static boolean validateCoordinate(int x, int y) {
	return 0 <= x && x < 9 && 0 <= y && y < 9;
    }

    public void fillCacheKey(Object[] key) {
	int k = 0;
	long pn = 0;
	int kai = 0, pnai = 9;
	for (int i = 0; i < 81; i++) {
	    var s = this.matrix[i];
	    k = k * 9 + s.ss.koma.ordinal();
	    if ((i + 1) % 9 == 0)
		key[kai++] = k;
	    pn = pn * 4 + (s.ss.nari ? 2 : 0) + s.ss.player.hashIndex;
	    if ((i + 1) % 27 == 0)
		key[pnai++] = pn;
	}
    }

    @Override
    public Ban clone() {
	var newMatrix = new MasuState[81];
	for (int i = 0; i < 81; i++)
	    newMatrix[i] = new MasuState(this.matrix[i]);
	return new Ban(newMatrix);
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Ban))
	    return false;
	var casted = (Ban) o;
	for (int i = 0; i < 81; i++)
	    if (this.matrix[0].ss.hash != casted.matrix[i].ss.hash)
		return false;
	return true;
    }

    @Override
    public int hashCode() {
	return Objects.hash((Object[]) matrix);
    }

    public static int generateId() {
	return idGen.getAndIncrement();
    }
}
