package begyyal.shogi.object;

import org.apache.commons.lang3.StringUtils;

import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList.ImmutableSuperList;
import begyyal.shogi.def.*;

@SuppressWarnings("preview")
public record MasuState(Player player, Koma koma, int suzi, int dan, boolean nariFlag) {
    
    public ImmutableSuperList<Vector> getTerritory(){
	return this.nariFlag ? koma.nariTerri : koma.territory;
    }
    
    /**
     * player(x/y) + koma(a~h) + nari(z/) <br>
     * バリデーション含む。
     * 
     * @param value 文字列値
     * @return マス状態
     */
    public static MasuState of(String value, int suzi, int dan) {
	
	var p = Player.of(value.substring(0, 1));
	if(p == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Player object.");
	
	var k = Koma.of(value.substring(1, 2));
	if(k == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Koma object.");
	
	boolean nari = value.length() > 2 && StringUtils.equals(value.substring(2, 3), "z");
	
	return new MasuState(p, k, suzi, dan, nari);
    }
}
