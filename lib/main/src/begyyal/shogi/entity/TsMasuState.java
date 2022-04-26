package begyyal.shogi.entity;

import java.util.Objects;

import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;

public class TsMasuState {
    public final Player player;
    public final TsKoma koma;
    public final int suzi;
    public final int dan;

    public TsMasuState(
	Player player,
	TsKoma koma,
	int suzi,
	int dan) {
	
	this.player = player;
	this.koma = koma;
	this.suzi = suzi;
	this.dan = dan;
	preValidate();
    }
    
    private void preValidate() {
	Objects.requireNonNull(player);
	Objects.requireNonNull(koma);
	if (suzi < 1 || suzi > 9 || dan < 1 || dan > 9)
	    throw new IllegalArgumentException("suzi/dan must be between 1 and 9.");
    }
}
