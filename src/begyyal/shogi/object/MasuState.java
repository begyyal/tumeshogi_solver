package begyyal.shogi.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import begyyal.commons.object.Pair;
import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XGen;
import begyyal.commons.object.collection.XList.ImmutableXList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.commons.util.cache.SimpleCacheResolver;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

// nullは入れない方針
public class MasuState {

    public static final MasuState Invalid = new MasuState(
	Player.None,
	Koma.Empty,
	-1,
	-1,
	false,
	false,
	Collections.emptySet());

    public final Player player;
    public final Koma koma;
    public final int x;
    public final int y;
    public final boolean nariFlag;
    public final boolean utu;
    public final Set<Pair<Integer, Integer>> rangedBy; // left=X,right=Y
    public final int cacheHash;

    public MasuState(MasuState s) {
	this(s.player, s.koma, s.x, s.y, s.nariFlag, s.utu, XGen.newHashSet(s.rangedBy));
    }

    public MasuState(
	Player player,
	Koma koma,
	int x,
	int y,
	boolean nariFlag,
	boolean utu,
	Set<Pair<Integer, Integer>> rangedBy) {

	this.player = player;
	this.koma = koma;
	this.x = x;
	this.y = y;
	this.nariFlag = nariFlag;
	this.utu = utu;
	this.rangedBy = rangedBy;
	this.cacheHash = ((31 + koma.ordinal()) * 31 + player.ordinal()) * 31 + (nariFlag ? 1 : 0);
    }

    public int getSuzi() {
	return 9 - x;
    }

    public int getDan() {
	return 9 - y;
    }

    public ImmutableXList<Vector> getTerritory() {
	return getTerritory(this.koma, this.nariFlag, this.player);
    }

    public ImmutableXList<Vector> getDecomposedTerritory() {
	return getDecomposedTerritory(this.koma, this.nariFlag, this.player);
    }

    public Vector getVectorTo(MasuState s) {
	return new Vector(s.x - this.x, s.y - this.y);
    }

    public boolean isEqualXY(MasuState s) {
	return s.x == this.x && s.y == this.y;
    }

    public boolean isEqualWithoutRange(MasuState s) {
	return this.isEqualXY(s)
		&& this.koma == s.koma
		&& this.player == s.player
		&& this.nariFlag == s.nariFlag;
    }

    public boolean isOpponentOu() {
	return player == Player.Opponent && koma == Koma.Ou;
    }

    public static MasuState emptyOf(int x, int y, Set<Pair<Integer, Integer>> rangedBy) {
	Objects.requireNonNull(rangedBy);
	return new MasuState(
	    Player.None,
	    Koma.Empty,
	    x,
	    y,
	    false,
	    false,
	    rangedBy);
    }

    public static ImmutableXList<Vector> getTerritory(
	Koma koma,
	boolean nariFlag,
	Player player) {
	if (player == Player.None)
	    return XListGen.empty();
	return player == Player.Self
		? (nariFlag ? koma.nariTerri : koma.territory)
		: (nariFlag ? koma.nariTerriRev : koma.territoryRev);
    }

    public static ImmutableXList<Vector> getDecomposedTerritory(
	Koma koma,
	boolean nariFlag,
	Player player) {
	var base = getTerritory(koma, nariFlag, player);
	return isLinearRange(koma, nariFlag)
		? SimpleCacheResolver.getAsPrivate(MasuState.class, 1,
		    ((31 + koma.ordinal()) * 31 + player.ordinal()) * 31 + (nariFlag ? 1 : 0),
		    () -> XListGen.immutableOf(base.stream()
			.flatMap(v -> Arrays.stream(v.decompose()))
			.toArray(Vector[]::new)))
		: base;
    }

    public static boolean isLinearRange(MasuState s) {
	return isLinearRange(s.koma, s.nariFlag);
    }

    public static boolean isLinearRange(Koma koma, boolean nariFlag) {
	return koma == Koma.Kyousha && !nariFlag ||
		koma == Koma.Hisha ||
		koma == Koma.Kaku;
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof MasuState))
	    return false;
	var casted = (MasuState) o;
	return this.isEqualWithoutRange(casted) && this.rangedBy.equals(casted.rangedBy);
    }

     @Override
     public int hashCode() {
	 return cacheHash;
     }
}
