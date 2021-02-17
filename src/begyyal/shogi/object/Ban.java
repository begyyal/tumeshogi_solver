package begyyal.shogi.object;

import java.util.function.Predicate;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;

public class Ban {

    private final MasuState[][] matrixOrigin;

    private Ban(String[] args) {
	// インデックスの振り順は将棋盤の読み方に倣う
	var matrix = new MasuState[9][9];
	for (String arg : args) {
	    int x = Integer.valueOf(arg.substring(0, 1));
	    int y = Integer.valueOf(arg.substring(1, 2));
	    matrix[x - 1][y - 1] = MasuState.of(arg.substring(2));
	}
	this.matrixOrigin = matrix; 
    }
    
    private SuperList<Integer> search(Predicate<MasuState> filter) {
	var indexList = SuperListGen.<Integer>newi();
	for(int x = 0; x < 9; x++) {
	    for(int y = 0; y < 9; y++) {
		if(filter.test(matrixOrigin[x][y]))
		    indexList.add(x*10 + y);
	    }
	}
	return indexList;
    }

    
    
    public static Ban of(String[] args) {
	return new Ban(args);
    }
}
