package begyyal.shogi.object;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class Ban implements Cloneable {

    // インデックスの振り順は将棋盤の読み方に倣わない。x/y座標で見る。
    private MasuState[][] matrix;

    private Ban(String[] args) {

	this.matrix = new MasuState[9][9];
	for (String arg : args) {
	    int x = Integer.valueOf(arg.substring(0, 1));
	    int y = Integer.valueOf(arg.substring(1, 2));
	    this.matrix[9 - x][9 - y] = MasuState.of(arg.substring(2), x, y);
	}

	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (this.matrix[x][y] == null)
		    emptyMasu(x, y, SuperListGen.newi());

	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		markRangeBy(this.matrix[x][y]);
    }

    private Ban(MasuState[][] matrix) {
	this.matrix = matrix;
    }

    public void markRangeBy(MasuState s) {
	for (var v : s.getTerritory())
	    for (var miniV : MatrixResolver.decompose(v)) {
		int vx = s.x() + miniV.x();
		int vy = s.y() + miniV.y();
		if (validateCoordinate(vx, vy))
		    this.matrix[vx][vy].rangedBy().add(s);
	    }
    }

    public void unmarkRangeBy(MasuState s) {
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		this.matrix[x][y].rangedBy().removeIf(state -> state.isEqualXY(s));
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
	int x = state.x(), y = state.y();
	int vx = x + v.x(), vy = y + v.y();
	return validateCoordinate(vx, vy) ? this.matrix[vx][vy] : MasuState.Invalid;
    }

    /**
     * 主体のマトリクスに対してfromからtoへの駒の移動を行う。<br>
     * 中間地点および移動先の検査無し。
     * 
     * @param from
     * @param to
     * @param player
     * @return 取得した駒。無ければnull
     */
    public Koma advance(MasuState from, MasuState to, Player player) {

	from.rangedBy().removeIf(s -> s.isEqualXY(to));
	emptyMasu(from.x(), from.y(), from.rangedBy());
	unmarkRangeBy(from);

	var occupied = this.matrix[to.x()][to.y()];
	advance(to);
	return occupied.koma() != Koma.Empty ? occupied.koma() : null;
    }

    /**
     * 主体のマトリクスに対して駒の配置を行う。<br>
     * 配置先の検査無し。
     * 
     * @param state
     */
    public void advance(MasuState state) {
	this.matrix[state.x()][state.y()] = state;
	markRangeBy(state);
    }

    /**
     * 主体のマトリクスに対して対象のステートを当てはめた際の特殊ルールの検査を行う。
     * 
     * @param state
     * @return 違反しなければtrue
     */
    public boolean validateState(MasuState state) {

	if (state.koma() == Koma.Hu) {
	    if (state.y() == 1)
		return false;
	    if (search(s -> s.x() == state.x()
		    && s.koma() == Koma.Hu
		    && s.player() == state.player())
			.findAny().isPresent())
		return false;

	} else if (state.koma() == Koma.Kyousha) {
	    if (state.y() == 1)
		return false;

	} else if (state.koma() == Koma.Keima) {
	    if (state.y() < 3)
		return false;
	}

	return true;
    }

    private void emptyMasu(int x, int y, SuperList<MasuState> rangedBy) {
	this.matrix[x][y] = MasuState.emptyOf(x, y, rangedBy);
    }

    private boolean validateCoordinate(int x, int y) {
	return 0 <= x && x < 9 && 0 <= y && y < 9;
    }

    public boolean isOuteBy(Player p, MasuState s) {
	return search(s2 -> s2.koma() == Koma.Ou && s2.player() != p)
	    .findFirst().get()
	    .rangedBy()
	    .anyMatch(s2 -> s.isEqualXY(s2));
    }

    @Override
    public Ban clone() {
	var newMatrix = new MasuState[9][9];
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		newMatrix[x][y] = this.matrix[x][y];
	return new Ban(newMatrix);
    }

    public static Ban of(String[] args) {
	return new Ban(args);
    }
}
