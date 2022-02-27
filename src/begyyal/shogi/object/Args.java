package begyyal.shogi.object;

import begyyal.commons.util.object.SuperList;
import begyyal.shogi.def.Koma;

public class Args {
    public final int numOfMoves;
    public final Ban initBan;
    public final SuperList<Koma> selfMotigoma;
    public final SuperList<Koma> opponentMotigoma;

    public Args(
	int numOfMoves,
	Ban initBan,
	SuperList<Koma> selfMotigoma,
	SuperList<Koma> opponentMotigoma) {
	this.numOfMoves = numOfMoves;
	this.initBan = initBan;
	this.selfMotigoma = selfMotigoma;
	this.opponentMotigoma = opponentMotigoma;
    }
}
