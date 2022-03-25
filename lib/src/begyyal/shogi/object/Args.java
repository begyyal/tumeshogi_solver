package begyyal.shogi.object;

public class Args {

    public final int numOfMoves;
    public final Ban initBan;
    public final MotigomaState[] motigoma;

    public Args(
	int numOfMoves,
	Ban initBan,
	MotigomaState[] motigoma) {

	this.numOfMoves = numOfMoves;
	this.initBan = initBan;
	this.motigoma = motigoma;
    }
}
