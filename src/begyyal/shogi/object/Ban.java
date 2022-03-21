package begyyal.shogi.object;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import begyyal.commons.object.Pair;
import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class Ban implements Cloneable {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    // インデックスの振り順は将棋盤の読み方に倣わない。x/y座標で見る。a
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
	if (s.koma == Koma.Empty)
	    return;
	boolean haveLinearRange = MasuState.isLinearRange(s);
	for (var v : s.getTerritory())
	    if (haveLinearRange) {
		for (var miniV : v.decompose())
		    if (!markRange(miniV, s.x, s.y))
			break;
	    } else
		markRange(v, s.x, s.y);
    }

    private boolean markRange(Vector v, int x, int y) {
	int vx = x + v.x;
	int vy = y + v.y;
	if (!validateCoordinate(vx, vy))
	    return false;
	this.matrix[vx * 9 + vy].rangedBy.add(Pair.of(x, y));
	return this.matrix[vx * 9 + vy].koma == Koma.Empty;
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
	int x = state.x, y = state.y;
	int vx = x + v.x, vy = y + v.y;
	return validateCoordinate(vx, vy) ? this.matrix[vx * 9 + vy] : MasuState.Invalid;
    }

    public MasuState advance(int fromX, int fromY, int toX, int toY, boolean tryNari) {

	var from = this.matrix[fromX * 9 + fromY];
	var to = this.matrix[toX * 9 + toY];

	if (!from.nariFlag && !tryNari && !validateState(from.koma, toX, toY, from.player))
	    return MasuState.Invalid;

	emptyMasu(fromX, fromY);

	var newState = new MasuState(
	    from.player,
	    from.koma,
	    toX,
	    toY,
	    from.nariFlag || tryNari && (from.player == Player.Self ? to.y > 5 : to.y < 3),
	    false,
	    to.rangedBy);
	this.matrix[toX * 9 + toY] = newState;

	refreshRange();

	return newState;
    }

    public MasuState deploy(Koma k, int x, int y, Player p) {

	if (!validateState(k, x, y, p) || k == Koma.Hu && !checkNihu(p, x))
	    return MasuState.Invalid;

	var state = new MasuState(p, k, x, y, false, true, this.matrix[x * 9 + y].rangedBy);
	this.matrix[x * 9 + y] = state;

	refreshRange();

	return state;
    }

    public boolean checkNihu(Player player, int x) {
	return search(s -> s.x == x
		&& s.koma == Koma.Hu
		&& !s.nariFlag
		&& s.player == player)
		    .findAny().isEmpty();
    }

    private void emptyMasu(int x, int y) {
	this.matrix[x * 9 + y] = MasuState.emptyOf(x, y, this.matrix[x * 9 + y].rangedBy);
    }

    public boolean checkingSafe() {
	return checkingSafe(search(MasuState::isOpponentOu).findFirst().get());
    }

    public boolean checkingSafe(MasuState ouState) {
	return ouState.rangedBy.stream()
	    .map(r -> getState(r.v1, r.v2))
	    .allMatch(s -> s.player == Player.Opponent);
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
	    k = k * 9 + s.koma.ordinal();
	    if ((i + 1) % 9 == 0)
		key[kai++] = k;
	    pn = pn * 4 + (s.nariFlag ? 2 : 0) + s.player.hashIndex;
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
	    if (!this.matrix[0].isEqualWithoutRange(casted.matrix[i]))
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
