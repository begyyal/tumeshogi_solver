package begyyal.shogi.entity;

import java.util.Objects;

import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;


public class TsMotigomaState {

    public final Player player;
    public final TsKoma koma;
    public final int num;

    public TsMotigomaState(
	Player player,
	TsKoma koma,
	int num) {
	
	this.player = player;
	this.koma = koma;
	this.num = num;
	preValidate();
    }
    
    private void preValidate() {
	Objects.requireNonNull(player);
	Objects.requireNonNull(koma);
	if (num < 0)
	    throw new IllegalArgumentException("motigoma num must be positive.");
	if (koma.nari)
	    throw new IllegalArgumentException("Narigoma in motigoma is not allowed.");
	if (koma == TsKoma.Ou)
	    throw new IllegalArgumentException("Ou in motigoma is not allowed.");
    }
}
