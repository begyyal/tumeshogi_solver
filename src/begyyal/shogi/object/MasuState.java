package begyyal.shogi.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import begyyal.commons.util.cache.SimpleCacheResolver;
import begyyal.commons.util.object.SuperList.ImmutableSuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.Vector;
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

    public final Player player;
    public final Koma koma;
    public final int x;
    public final int y;
    public final boolean nariFlag;
    public final Set<Pair<Integer, Integer>> rangedBy; // left=X,right=Y

    public MasuState(MasuState s) {
	this(s.player, s.koma, s.x, s.y, s.nariFlag, Sets.newHashSet(s.rangedBy));
    }

    public MasuState(
	Player player,
	Koma koma,
	int x,
	int y,
	boolean nariFlag,
	Set<Pair<Integer, Integer>> rangedBy) {

	this.player = player;
	this.koma = koma;
	this.x = x;
	this.y = y;
	this.nariFlag = nariFlag;
	this.rangedBy = rangedBy;
    }

    public int getSuzi() {
	return 9 - x;
    }

    public int getDan() {
	return 9 - y;
    }

    public ImmutableSuperList<Vector> getTerritory() {
	return getTerritory(this.koma, this.nariFlag, this.player);
    }

    public ImmutableSuperList<Vector> getDecomposedTerritory() {
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

    public static MasuState emptyOf(int suzi, int dan, Set<Pair<Integer, Integer>> rangedBy) {
	return new MasuState(
	    Player.None,
	    Koma.Empty,
	    suzi,
	    dan,
	    false,
	    rangedBy);
    }

    public static ImmutableSuperList<Vector> getTerritory(
	Koma koma,
	boolean nariFlag,
	Player player) {
	if (player == Player.None)
	    return SuperListGen.empty();
	return player == Player.Self
		? (nariFlag ? koma.nariTerri : koma.territory)
		: (nariFlag ? koma.nariTerriRev : koma.territoryRev);
    }

    public static ImmutableSuperList<Vector> getDecomposedTerritory(
	Koma koma,
	boolean nariFlag,
	Player player) {
	var base = getTerritory(koma, nariFlag, player);
	return isLinearRange(koma, nariFlag)
		? SimpleCacheResolver.getAsPrivate(MasuState.class, 1,
		    Triple.of(koma, nariFlag, player),
		    () -> SuperListGen.immutableOf(base.stream()
			.flatMap(v -> Arrays.stream(v.decompose())).toArray(Vector[]::new)))
		: base;
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
	if (p == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Player object.");

	var k = Koma.of(value.substring(1, 2));
	if (k == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Koma object.");

	boolean nari = value.length() > 2 && StringUtils.equals(value.substring(2, 3), "z");

	return new MasuState(p, k, 9 - suzi, 9 - dan, nari, Sets.newHashSet());
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

	if (this.isEqualWithoutRange(casted) && this.rangedBy.size() == casted.rangedBy.size()) {
	    var a = Lists.newArrayList(this.rangedBy);
	    var b = Lists.newArrayList(casted.rangedBy);
	    Collections.sort(a);
	    Collections.sort(b);
	    return CollectionUtils.isEqualCollection(a, b);
	} else
	    return false;
    }
}
