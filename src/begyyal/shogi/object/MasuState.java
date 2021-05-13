package begyyal.shogi.object;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.ImmutableSuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

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

    public static final ConcurrentHashMap<Pair<Koma, Boolean>, ImmutableSuperList<Vector>> ReverseTerritoryCache = //
	    new ConcurrentHashMap<Pair<Koma, Boolean>, ImmutableSuperList<Vector>>();

    public int getSuzi() {
	return 9 - x;
    }

    public int getDan() {
	return 9 - y;
    }

    public ImmutableSuperList<Vector> getTerritory() {
	var base = nariFlag ? koma.nariTerri : koma.territory;
	return player == Player.Opponent
		? ReverseTerritoryCache.computeIfAbsent(Pair.of(koma, nariFlag),
		    k -> SuperListGen.immutableOf(
			base.stream().map(v -> v.reverse(false, true)).toArray(Vector[]::new)))
		: base;
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
	if (p == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Player object.");

	var k = Koma.of(value.substring(1, 2));
	if (k == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Koma object.");

	boolean nari = value.length() > 2 && StringUtils.equals(value.substring(2, 3), "z");

	return new MasuState(p, k, 9 - suzi, 9 - dan, nari, SuperListGen.newi());
    }

    // rangedByの循環比較を抑止するために上書きする。
    // rangedByが含むMasuStateのrangedByは本質的には第三者的要素であるため、比較を無視しても差支えない。
    @Override
    public boolean equals(Object o) {
	if (!(o instanceof MasuState))
	    return false;
	var casted = (MasuState) o;
	return this.isEqualWithoutRange(casted)
		&& this.rangedBy.size() == casted.rangedBy.size()
		&& this.rangedBy.zip(casted.rangedBy)
		    .allMatch(p -> p.getLeft().isEqualWithoutRange(p.getRight()));
    }
}
