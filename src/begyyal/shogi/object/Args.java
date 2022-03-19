package begyyal.shogi.object;

import begyyal.commons.object.collection.XList;
import begyyal.shogi.def.Koma;

public class Args {
    public final int numOfMoves;
    public final Ban initBan;
    public final XList<Koma> selfMotigoma;
    public final XList<Koma> opponentMotigoma;

    public Args(
	int numOfMoves,
	Ban initBan,
	XList<Koma> selfMotigoma,
	XList<Koma> opponentMotigoma) {
	this.numOfMoves = numOfMoves;
	this.initBan = initBan;
	this.selfMotigoma = selfMotigoma;
	this.opponentMotigoma = opponentMotigoma;
    }
}
