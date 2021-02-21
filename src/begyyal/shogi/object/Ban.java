package begyyal.shogi.object;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class Ban implements Cloneable {

    private MasuState[][] matrix;

    private Ban(String[] args) {
	// インデックスの振り順は将棋盤の読み方に倣う
	var matrix = new MasuState[9][9];
	for (String arg : args) {
	    int x = Integer.valueOf(arg.substring(0, 1));
	    int y = Integer.valueOf(arg.substring(1, 2));
	    matrix[x - 1][y - 1] = MasuState.of(arg.substring(2), x, y);
	}
	this.matrix = matrix;
    }

    private Ban(MasuState[][] matrix) {
	this.matrix = matrix;
    }

    public SuperList<MasuState> search(Predicate<MasuState> filter) {
	var stateList = SuperListGen.<MasuState>newi();
	for (int x = 0; x < 9; x++)
	    for (int y = 0; y < 9; y++)
		if (filter.test(matrix[x][y]))
		    stateList.add(matrix[x][y]);
	return stateList;
    }

    public SuperList<MasuState> serializeMatrix() {
	return Arrays.stream(this.matrix).flatMap(l -> Arrays.stream(l))
		.collect(SuperListGen.collect());
    }

    /**
     * 主体のマトリクスを1手進める。
     * 
     * @param state
     * @param v
     * @param player
     * @param motigomaBucket
     * @return １手も進められなかった場合にfalse
     */
    public boolean advanceItte(MasuState state, Vector v, Player player, Koma[] motigomaBucket) {

	int x = state.suzi() - 1, y = state.dan() - 1;
	int vx = v.x(), vy = v.y();
	int[] result = new int[] { -1, -1 }; // 0=x,1=y

	if (vx != 1 && vx != -1 && vy != 1 && vy != -1) {
	    
	    int start = 0, distance = 0;
	    Function<Integer, Integer> xSupplier = i -> i, ySupplier = i -> i;
	    if (vx == vy) {
		start = x;
		distance = x + vx;
	    } else if (vx == 0) {
		start = y;
		distance = y + vy;
		xSupplier = i -> x;
	    } else if (vy == 0) {
		start = x;
		distance = x + vx;
		ySupplier = i -> y;
	    }

	    for (int i : MatrixResolver.vectorOrderedStream(start, distance).toArray())
		if (this.advanceItimasu(
			result, 
			xSupplier.apply(i), 
			ySupplier.apply(i), 
			player,
			motigomaBucket))
		    break;
	}

	if (result[0] == -1)
	    return false;

	var destState = new MasuState(
		player,
		state.koma(),
		result[0] + 1,
		result[1] + 1,
		state.nariFlag() || result[y] < 3);
	this.matrix[x][y] = null;
	this.matrix[result[0]][result[1]] = destState;

	return true;
    }

    // trueで一手終了
    private boolean
	advanceItimasu(int[] result, int x, int y, Player player, Koma[] motigomaBucket) {

	var dest = this.matrix[x][y];
	if (dest == null) {
	    result[0] = x;
	    result[1] = y;
	    return false;
	} else if (dest.player() != player) {
	    result[0] = x;
	    result[1] = y;
	    this.matrix[x][y] = null;
	    motigomaBucket[0] = dest.koma();
	}
	return true;
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
