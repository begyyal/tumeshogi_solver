package begyyal.shogi.object;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import begyyal.commons.object.Vector;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.TryNari;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.object.MasuState.SmartMasuState;

public class Ban implements Cloneable {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    // インデックスの振り順は将棋盤の読み方に倣わない。x/y座標で見る。
    private MasuState[] matrix;

    public Ban(MasuState[] matrix) {
	this.matrix = matrix;
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
	boolean haveLinearRange = s.ss.koma.isLinearRange();
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

    public MasuState exploration(SmartMasuState ss, Vector v) {
	int x = ss.x, y = ss.y;
	int vx = x + v.x, vy = y + v.y;
	return validateCoordinate(vx, vy) ? this.matrix[vx * 9 + vy] : null;
    }

    public KihuRecord advance(int fromX, int fromY, int toX, int toY, TryNari tn) {

	var from = this.matrix[fromX * 9 + fromY];
	var fss = from.ss;
	var to = this.matrix[toX * 9 + toY];

	boolean naru = tn == TryNari.Ru;
	if (!naru && !validateState(fss.koma, toX, toY, fss.player))
	    return null;

	var te = KihuRecord.resolveAdvance(to, fss.player, fss.koma, fromX, fromY, tn);

	emptyMasu(fromX, fromY);

	var newState = new MasuState(
	    fss.player,
	    naru ? fss.koma.naru() : fss.koma,
	    toX,
	    toY,
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

	var newState = new MasuState(p, k, x, y, state.rangedBy);
	this.matrix[x * 9 + y] = newState;

	refreshRange();

	return te;
    }

    public boolean checkNihu(Player player, int x) {
	return search(s -> s.ss.x == x
		&& s.ss.koma == Koma.Hu
		&& s.ss.player == player)
		    .findAny().isEmpty();
    }

    private void emptyMasu(int x, int y) {
	this.matrix[x * 9 + y] = MasuState.emptyOf(x, y, this.matrix[x * 9 + y].rangedBy);
    }

    public boolean checkOute() {
	return !search(MasuState::isGyoku).findFirst().get().checkSafe(Player.Gote);
    }

    public static boolean validateState(Koma koma, int x, int y, Player p) {
	int end = p == Player.Sente ? 8 : 0;
	return (koma != Koma.Hu && koma != Koma.Kyousya || y != end)
		&& (koma != Koma.Keima || (p == Player.Sente ? y < 7 : y > 1));
    }

    private static boolean validateCoordinate(int x, int y) {
	return 0 <= x && x < 9 && 0 <= y && y < 9;
    }

    public void fillCacheKey(Object[] key) {
	long k = 1;
	long pn = 1;
	int ki = 0;
	for (int i = 1; i <= 81; i++) {
	    var s = this.matrix[i - 1];
	    k = k * 15 + s.ss.koma.ordinal();
	    if (i % 12 == 0) {
		key[ki++] = k;
		k = 1;
	    }
	    pn = pn * 3 + (s.ss.player == null ? 2 : s.ss.player.ordinal());
	    if (i % 41 == 0) {
		key[ki++] = pn;
		pn = 1;
	    }
	}
	key[ki++] = k;
	key[ki] = pn;
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
