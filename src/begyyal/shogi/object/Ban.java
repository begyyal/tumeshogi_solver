package begyyal.shogi.object;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.function.SuperStrings;
import begyyal.commons.util.math.SuperMath;
import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.PairList;
import begyyal.commons.util.object.PairList.PairListGen;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class Ban implements Cloneable {

    private static final String banArgRegex = "([1-9][1-9][xy][a-dfg][z]?|[1-9][1-9][xy][eh])+";

    // インデックスの振り順は将棋盤の読み方に倣わない。x/y座標で見る。
    private MasuState[][] matrix;

    private Ban(String arg) {

	if (!arg.matches(banArgRegex))
	    throw new IllegalArgumentException("Ban argument format is invalid.");

	this.matrix = new MasuState[9][9];

	String draft = arg;
	while (!draft.isBlank()) {

	    int skipIndex = SuperStrings.firstIndexOf(draft, "x", "y").getRight();
	    var next = SuperStrings.firstIndexOf(draft.substring(skipIndex + 1), "x", "y");
	    int kiritori = next == null ? draft.length() : next.getRight() + skipIndex - 1;
	    var masu = draft.substring(0, kiritori);
	    draft = kiritori == draft.length() ? Strs.empty : draft.substring(kiritori);

	    int x = Integer.valueOf(masu.substring(0, 1));
	    int y = Integer.valueOf(masu.substring(1, 2));
	    this.matrix[9 - x][9 - y] = MasuState.of(masu.substring(2), x, y);
	}

	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (this.matrix[x][y] == null)
		    emptyMasu(x, y, PairListGen.newi());

	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		markRangeBy(this.matrix[x][y]);

	validateCondition();
    }

    private Ban(MasuState[][] matrix) {
	this.matrix = matrix;
    }

    private void validateCondition() {

	boolean existOu = false;
	for (int x = 0; x < 9; x++) {
	    int huCountX = 0, huCountY = 0;
	    for (int y = 0; y < 9; y++) {
		var state = this.matrix[x][y];
		if (state.koma == Koma.Empty)
		    continue;
		existOu = existOu || state.koma == Koma.Ou && state.player == Player.Opponent;
		if (state.koma == Koma.Hu && !state.nariFlag
			&& (state.player == Player.Self ? ++huCountX == 2 : ++huCountY == 2))
		    throw new IllegalArgumentException("It's 2hu.");
		if (!state.nariFlag && !validateState(state.koma, x, y, state.player))
		    throw new IllegalArgumentException("There is invalid arrangement.");
	    }
	}

	if (!existOu)
	    throw new IllegalArgumentException("There must be the koma [Ou] in y's arguments.");
    }

    public MasuState getState(int x, int y) {
	return this.matrix[x][y];
    }

    public void markRangeBy(MasuState s) {
	for (var v : s.getTerritory())
	    for (var miniV : MatrixResolver.decompose(v)) {
		int vx = s.x + miniV.x();
		int vy = s.y + miniV.y();
		if (!validateCoordinate(vx, vy))
		    break;
		this.matrix[vx][vy].rangedBy.add(s.x, s.y);
		if (this.matrix[vx][vy].koma != Koma.Empty)
		    break;
	    }
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

    /**
     * 進行先のステートを取得する。
     * 
     * @param state
     * @param v
     * @return ステート
     */
    public MasuState exploration(MasuState state, Vector v) {
	int x = state.x, y = state.y;
	int vx = x + v.x(), vy = y + v.y();
	return validateCoordinate(vx, vy) ? this.matrix[vx][vy] : MasuState.Invalid;
    }

    /**
     * 主体のマトリクスに対してfromからtoへの指定座標への駒の移動を行う。<br>
     * 中間地点および移動先の検査無し。
     * 
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @param tryNari
     * @return 取得した駒。無ければnull
     */
    public Koma advance(int fromX, int fromY, int toX, int toY, boolean tryNari) {

	var from = this.matrix[fromX][fromY];
	var to = this.matrix[toX][toY];

	if (to.koma != Koma.Empty)
	    unmarkRangeBy(toX, toY);
	emptyMasu(fromX, fromY);
	unmarkRangeBy(fromX, fromY);

	var newState = new MasuState(
	    from.player,
	    from.koma,
	    toX,
	    toY,
	    from.nariFlag || to.y <= 3,
	    to.rangedBy);
	var occupied = this.matrix[to.x][to.y];

	this.matrix[toX][toY] = newState;
	markRangeBy(newState);
	cutoffRangeBy(newState);

	return occupied.koma != Koma.Empty ? occupied.koma : null;
    }

    public MasuState deploy(Koma k, int x, int y, Player p) {

	if (!validateState(k, x, y, p) || k == Koma.Hu && !checkNihu(p, x))
	    return MasuState.Invalid;
	var state = new MasuState(p, k, x, y, false, this.matrix[x][y].rangedBy);

	this.matrix[x][y] = state;
	markRangeBy(state);
	cutoffRangeBy(state);

	return state;
    }

    private void cutoffRangeBy(MasuState newState) {

	newState.rangedBy
	    .stream()
	    .map(p -> this.matrix[p.getLeft()][p.getRight()])
	    .filter(s -> MasuState.isLinearRange(s))
	    .forEach(s -> {

		var v = s.getVectorTo(newState);
		int mltX = SuperMath.simplify(v.x()), mltY = SuperMath.simplify(v.y());
		int x = newState.x, y = newState.y;

		while (validateCoordinate(x += mltX, y += mltY)) {
		    var s2 = this.matrix[x][y];
		    s2.rangedBy.removeIf(p -> p.getLeft() == s.x && p.getRight() == s.y);
		    if (s2.koma != Koma.Empty)
			break;
		}
	    });
    }

    /**
     * 対象の座標に対象の駒の配置が可能か検証を行う。(2歩を除く静的なもの)
     * 
     * @param koma
     * @param x
     * @param y
     * @return 違反しなければtrue
     */
    public static boolean validateState(Koma koma, int x, int y, Player p) {
	int end = p == Player.Self ? 8 : 0;
	return (koma != Koma.Hu && koma != Koma.Kyousha || y != end)
		&& (koma != Koma.Keima || (p == Player.Self ? y < 7 : y > 1));
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

    private boolean validateCoordinate(int x, int y) {
	return 0 <= x && x < 9 && 0 <= y && y < 9;
    }

    public boolean isOuteBy(Player p, int x, int y) {
	return search(s -> s.koma == Koma.Ou && s.player != p)
	    .findFirst().get().rangedBy.anyMatch(s -> s.getLeft() == x && s.getRight() == y);
    }

    @Override
    public Ban clone() {
	var newMatrix = new MasuState[9][9];
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		newMatrix[x][y] = new MasuState(this.matrix[x][y]);
	return new Ban(newMatrix);
    }

    public static Ban of(String arg) {
	return new Ban(arg);
    }
}
