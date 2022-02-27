package begyyal.shogi.object;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.PairList;
import begyyal.commons.util.object.PairList.PairListGen;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class Ban implements Cloneable {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();

    // インデックスの振り順は将棋盤の読み方に倣わない。x/y座標で見る。
    private MasuState[][] matrix;

    public Ban(MasuState[][] matrix) {
	this.matrix = matrix;
    }

    public void setup() {
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (this.matrix[x][y] == null)
		    emptyMasu(x, y, PairListGen.newi());
	markRangeAll();
    }

    public MasuState getState(int x, int y) {
	return this.matrix[x][y];
    }

    private void refreshRange() {
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		this.matrix[x][y].rangedBy.clear();
	markRangeAll();
    }

    private void markRangeAll() {
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (this.matrix[x][y].koma != Koma.Empty)
		    markRangeBy(this.matrix[x][y]);
    }

    public void markRangeBy(MasuState s) {
	boolean haveLinearRange = MasuState.isLinearRange(s);
	for (var v : s.getTerritory())
	    if (haveLinearRange) {
		for (var miniV : MatrixResolver.decompose(v))
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
	this.matrix[vx][vy].rangedBy.add(x, y);
	return this.matrix[vx][vy].koma == Koma.Empty;
    }

    public void unmarkRangeBy(int targetX, int targetY) {
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		this.matrix[x][y].rangedBy
		    .removeIf(p -> p.getLeft() == targetX && p.getRight() == targetY);
    }

    public Stream<MasuState> search(Predicate<MasuState> filter) {
	var result = new MasuState[81];
	int count = 0;
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (filter.test(matrix[x][y]))
		    result[count++] = matrix[x][y];
	return count == 0 ? Stream.empty() : Arrays.stream(result, 0, count);
    }

    public SuperList<MasuState> serializeMatrix() {
	return Arrays.stream(this.matrix).flatMap(l -> Arrays.stream(l))
	    .collect(SuperListGen.collect());
    }

    public MasuState exploration(MasuState state, Vector v) {
	int x = state.x, y = state.y;
	int vx = x + v.x, vy = y + v.y;
	return validateCoordinate(vx, vy) ? this.matrix[vx][vy] : MasuState.Invalid;
    }

    public MasuState advance(int fromX, int fromY, int toX, int toY, boolean tryNari) {

	var from = this.matrix[fromX][fromY];
	var to = this.matrix[toX][toY];

	if (!from.nariFlag && !tryNari && !validateState(from.koma, toX, toY, from.player))
	    return MasuState.Invalid;

	emptyMasu(fromX, fromY);

	var newState = new MasuState(
	    from.player,
	    from.koma,
	    toX,
	    toY,
	    from.nariFlag || (tryNari && from.player == Player.Self ? to.y > 5 : to.y < 3),
	    to.rangedBy);
	this.matrix[toX][toY] = newState;

	refreshRange();
	return newState;
    }

    public MasuState deploy(Koma k, int x, int y, Player p) {

	if (!validateState(k, x, y, p) || k == Koma.Hu && !checkNihu(p, x))
	    return MasuState.Invalid;

	var state = new MasuState(p, k, x, y, false, this.matrix[x][y].rangedBy);
	this.matrix[x][y] = state;

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
	emptyMasu(x, y, this.matrix[x][y].rangedBy);
    }

    private void emptyMasu(int x, int y, PairList<Integer, Integer> rangedBy) {
	this.matrix[x][y] = MasuState.emptyOf(x, y, rangedBy);
    }

    public boolean checkingSafe() {
	return checkingSafe(search(MasuState::isOpponentOu).findFirst().get());
    }

    public boolean checkingSafe(MasuState ouState) {
	return ouState.rangedBy
	    .stream()
	    .map(r -> getState(r.getLeft(), r.getRight()))
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

    @Override
    public Ban clone() {
	var newMatrix = new MasuState[9][9];
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		newMatrix[x][y] = new MasuState(this.matrix[x][y]);
	return new Ban(newMatrix);
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Ban))
	    return false;
	var casted = (Ban) o;
	return this.id == casted.id;
    }

    public static int generateId() {
	return idGen.getAndIncrement();
    }
}
