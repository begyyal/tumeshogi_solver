package begyyal.shogi.cmd.object;

import java.util.Set;

import begyyal.shogi.entity.TsMasuState;
import begyyal.shogi.entity.TsMotigomaState;

public class CmdArgs {

    public final int numOfMoves;
    public final Set<TsMasuState> ban;
    public final Set<TsMotigomaState> motigoma;
    public final boolean translate;
    public final boolean dispTime;

    public CmdArgs(
	int numOfMoves,
	Set<TsMasuState> ban,
	Set<TsMotigomaState> motigoma,
	boolean translate,
	boolean dispTime) {

	this.numOfMoves = numOfMoves;
	this.ban = ban;
	this.motigoma = motigoma;
	this.translate = translate;
	this.dispTime = dispTime;
    }
}
