package begyyal.shogi.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

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
	Collections.emptySet());

    public final SmartMasuState ss;
    public final Set<SmartMasuState> rangedBy;

    public MasuState(MasuState s) {
	this.ss = s.ss;
	this.rangedBy = XGen.newHashSet(s.rangedBy);
    }

    public MasuState(
	Player player,
	Koma koma,
	int x,
	int y,
	boolean nariFlag,
	Set<SmartMasuState> rangedBy) {

	this.ss = new SmartMasuState(player, koma, x, y, nariFlag);
	this.rangedBy = rangedBy;
    }

    public ImmutableXList<Vector> getTerritory() {
	return getTerritory(this.ss.koma, this.ss.nari, this.ss.player);
    }

    public ImmutableXList<Vector> getDecomposedTerritory() {
	return getDecomposedTerritory(this.ss.koma, this.ss.nari, this.ss.player);
    }

    public Vector getVectorTo(MasuState s) {
	return new Vector(s.ss.x - this.ss.x, s.ss.y - this.ss.y);
    }

    public boolean isOpponentOu() {
	return ss.player == Player.Opponent && ss.koma == Koma.Ou;
    }

    public boolean checkSafe(Player p) {
	return this.rangedBy.stream().allMatch(s -> s.player == p);
    }

    public static MasuState emptyOf(int x, int y, Set<SmartMasuState> rangedBy) {
	Objects.requireNonNull(rangedBy);
	return new MasuState(
	    Player.None,
	    Koma.Empty,
	    x,
	    y,
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
	return isLinearRange(s.ss.koma, s.ss.nari);
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
	return this.ss.hash == casted.ss.hash && this.rangedBy.equals(casted.rangedBy);
    }

    @Override
    public int hashCode() {
	return ss.hash;
    }

    public class SmartMasuState {

	public final Player player;
	public final Koma koma;
	public final int x;
	public final int y;
	public final boolean nari;
	public final int hash;

	private SmartMasuState(SmartMasuState s) {
	    this(s.player, s.koma, s.x, s.y, s.nari);
	}

	private SmartMasuState(
	    Player player,
	    Koma koma,
	    int x,
	    int y,
	    boolean nariFlag) {

	    this.player = player;
	    this.koma = koma;
	    this.x = x;
	    this.y = y;
	    this.nari = nariFlag;
	    this.hash = ((((9 + x)
		    * 9 + y)
		    * 9 + koma.ordinal())
		    * 3 + player.ordinal())
		    * 2 + (nariFlag ? 1 : 0);
	}

	public boolean isEqualXY(SmartMasuState s) {
	    return s.x == this.x && s.y == this.y;
	}

	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof SmartMasuState))
		return false;
	    var casted = (SmartMasuState) o;
	    return this.hash == casted.hash;
	}

	@Override
	public int hashCode() {
	    return hash;
	}
    }
}
