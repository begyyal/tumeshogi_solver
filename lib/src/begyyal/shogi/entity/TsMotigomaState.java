package begyyal.shogi.entity;

import java.util.Objects;

import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;


public class TsMotigomaState {

    public final TsKoma koma;
    public final Player player;
    public final int num;

    public TsMotigomaState(
	TsKoma koma,
	Player player,
	int num) {
	
	this.koma = koma;
	this.player = player;
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
