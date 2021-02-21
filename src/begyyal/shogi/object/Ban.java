package begyyal.shogi.object;

import java.util.Arrays;
import java.util.function.Predicate;

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
     * 主体のマトリクスを1手進める。<br> 
     * 中間地点の探査無し。
     * 
     * @param state
     * @param v
     * @param player
     * @param motigomaBucket
     * @return 進行状況
     */
    public AdvanceState advanceItte(MasuState state, Vector v, Player player, Koma[] motigomaBucket) {

	int x = state.suzi() - 1, y = state.dan() - 1;
	int vx = x + v.x(), vy = y + v.y();
	if (validateCoordinate(vx, vy))
	    return AdvanceState.NoPassage;
	
	AdvanceState result;
	var dest = this.matrix[vx][vy];
	if (dest != null) {
	    if (dest.player() != player) {
		motigomaBucket[0] = dest.koma();
		result = AdvanceState.KnockDown;
	    } else
		return AdvanceState.NoPassage;
	} else
	    result = AdvanceState.None;

	var destState = new MasuState(
		player,
		state.koma(),
		vx + 1,
		vy + 1,
		state.nariFlag() || vy < 3);
	this.matrix[x][y] = null;
	this.matrix[vx][vy] = destState;

	return result;
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
    
    public enum AdvanceState {
	None,
	NoPassage,
	KnockDown;
    }
}
