package begyyal.shogi.object;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XGen;
import begyyal.commons.object.collection.XList.ImmutableXList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.commons.util.cache.SimpleCacheResolver;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.common.Player;

public class MasuState {

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
	Set<SmartMasuState> rangedBy) {

	this.ss = new SmartMasuState(player, koma, x, y);
	this.rangedBy = rangedBy;
    }

    public ImmutableXList<Vector> getTerritory() {
	return getTerritory(this.ss.koma, this.ss.player);
    }

    public ImmutableXList<Vector> getDecomposedTerritory() {
	return getDecomposedTerritory(this.ss.koma, this.ss.player);
    }

    public Vector getVectorTo(SmartMasuState s) {
	return new Vector(s.x - this.ss.x, s.y - this.ss.y);
    }

    public boolean isOpponentOu() {
	return ss.player == Player.Gote && ss.koma == Koma.Ou;
    }

    public boolean checkSafe(Player p) {
	return this.rangedBy.stream().allMatch(s -> s.player == p);
    }

    public static MasuState emptyOf(int x, int y, Set<SmartMasuState> rangedBy) {
	Objects.requireNonNull(rangedBy);
	return new MasuState(
	    null,
	    Koma.Empty,
	    x,
	    y,
	    rangedBy);
    }

    public static ImmutableXList<Vector> getTerritory(Koma koma, Player player) {
	return player == null ? XListGen.empty()
		: player == Player.Sente ? koma.territory : koma.territoryRev;
    }

    public static ImmutableXList<Vector> getDecomposedTerritory(Koma koma, Player player) {
	var base = getTerritory(koma, player);
	return isLinearRange(koma)
		? SimpleCacheResolver.getAsPrivate(MasuState.class, 1,
		    (16 + koma.ordinal()) * 2 + player.ordinal(),
		    () -> XListGen.immutableOf(base.stream()
			.flatMap(v -> Arrays.stream(v.decompose()))
			.toArray(Vector[]::new)))
		: base;
    }

    public boolean isLinearRange() {
	return isLinearRange(this.ss.koma);
    }

    public static boolean isLinearRange(Koma koma) {
	return koma == Koma.Kyousya ||
		koma == Koma.Hisya || koma == Koma.Ryuu ||
		koma == Koma.Kaku || koma == Koma.Uma;
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
	public final int hash;

	private SmartMasuState(SmartMasuState s) {
	    this(s.player, s.koma, s.x, s.y);
	}

	private SmartMasuState(
	    Player player,
	    Koma koma,
	    int x,
	    int y) {

	    this.player = player;
	    this.koma = koma;
	    this.x = x;
	    this.y = y;
	    this.hash = ((((9 + x)
		    * 9 + y)
		    * 16 + koma.ordinal())
		    * 3 + (player == null ? 2 : player.ordinal()));
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
