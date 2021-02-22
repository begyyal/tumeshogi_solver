package begyyal.shogi.object;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class Ban implements Cloneable {

    // 空ステートは利用頻度が高いので取り置き
    private static final MasuState[][] EmptyMatrix;
    static {
	var matrix = new MasuState[9][9];
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		matrix[x][y] = MasuState.emptyOf(x + 1, y + 1);
	EmptyMatrix = matrix;
    }

    private MasuState[][] matrix;

    private Ban(String[] args) {
	// インデックスの振り順は将棋盤の読み方に倣う
	var matrix = new MasuState[9][9];
	for (String arg : args) {
	    int x = Integer.valueOf(arg.substring(0, 1));
	    int y = Integer.valueOf(arg.substring(1, 2));
	    matrix[x - 1][y - 1] = MasuState.of(arg.substring(2), x, y);
	}
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (matrix[x][y] == null)
		    matrix[x][y] = EmptyMatrix[x][y];

	this.matrix = matrix;
    }

    private Ban(MasuState[][] matrix) {
	this.matrix = matrix;
    }

    public Stream<MasuState> search(Predicate<MasuState> filter) {
	var result = new MasuState[81];
	int count = 0;
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (filter.test(matrix[x][y]) && ++count != 0)
		    result[x * 9 + y] = matrix[x][y];
	return Arrays.stream(result, 0, count);
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
	int x = state.suzi() - 1, y = state.dan() - 1;
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

	emptyMasu(from.suzi(), from.dan());
	this.matrix[to.suzi() - 1][to.dan() - 1] = new MasuState(
	    player,
	    from.koma(),
	    to.suzi(),
	    to.dan(),
	    from.nariFlag() || to.dan() <= 3);

	return to.koma() != Koma.Empty ? to.koma() : null;
    }

    /**
     * 主体のマトリクスに対して対象のステートを当てはめた際の特殊ルールの検査を行う。
     * 
     * @param state
     * @return 違反しなければtrue
     */
    public boolean validateState(MasuState state) {

	if (state.koma() == Koma.Hu) {
	    if (state.dan() == 1)
		return false;
	    if (search(s -> s.suzi() == state.suzi()
		    && s.koma() == Koma.Hu
		    && s.player() == state.player())
			.findAny().isPresent())
		return false;

	} else if (state.koma() == Koma.Kyousha) {
	    if (state.dan() == 1)
		return false;

	} else if (state.koma() == Koma.Keima) {
	    if (state.dan() < 3)
		return false;
	}

	return true;
    }

    private void emptyMasu(int suzi, int dan) {
	this.matrix[suzi - 1][dan - 1] = EmptyMatrix[suzi - 1][dan - 1];
    }

    private boolean validateCoordinate(int x, int y) {
	return 0 <= x && x < 9 && 0 <= y && y < 9;
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
