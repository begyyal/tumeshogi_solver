package begyyal.shogi.object;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public class MotigomaState {

    public final Koma koma;
    public final Player player;
    private final int partialHash;
    public int num;

    public MotigomaState(Koma koma, Player player, int num) {
	this.koma = koma;
	this.player = player;
	this.partialHash = (31 + koma.hashCode()) * 31 + player.hashCode();
	this.num = num;
    }
    
    @Override
    public boolean equals(Object o) {
	if (!(o instanceof MotigomaState))
	    return false;
	var casted = (MotigomaState) o;
	return this.koma == casted.koma && this.player == casted.player && this.num == casted.num;
    }

    @Override
    public int hashCode() {
	return partialHash * 31 + num;
    }
}
