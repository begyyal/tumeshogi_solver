package begyyal.shogi.object;

import org.apache.commons.lang3.StringUtils;

import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.ImmutableSuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.*;

// nullは入れない方針
@SuppressWarnings("preview")
public record MasuState(
    Player player, 
    Koma koma, 
    int x, 
    int y, 
    boolean nariFlag, 
    SuperList<MasuState> rangedBy) {
    
    public static final MasuState Invalid = new MasuState(
	Player.None, 
	Koma.Empty, 
	-1, 
	-1, 
	false,
	SuperListGen.empty()); 
    
    public int getSuzi() {
	return 9 - x;
    }

    public int getDan() {
	return 9 - y;
    }
    
    public ImmutableSuperList<Vector> getTerritory(){
	return this.nariFlag ? koma.nariTerri : koma.territory;
    }
    
    public Vector getVectorTo(MasuState s) {
	return new Vector(s.x - this.x, s.y - this.y);
    }
    
    public boolean isEqualXY(MasuState s) {
	return s.x == this.x && s.y == this.y;
    }

    public static MasuState emptyOf(int suzi, int dan, SuperList<MasuState> rangedBy) {
	return new MasuState(
	    Player.None, 
	    Koma.Empty, 
	    suzi, 
	    dan, 
	    false,
	    rangedBy);
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
	
	return new MasuState(p, k, 9 - suzi, 9 - dan, nari, SuperListGen.newi());
    }
}
