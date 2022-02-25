package begyyal.shogi.def;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import begyyal.commons.util.object.SuperList.ImmutableSuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.Vector;

public enum Koma {

    Hu(
	"a",
	SuperListGen.immutableOf(new Vector(0, 1)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	18),
    Kyousha(
	"b",
	SuperListGen.immutableOf(new Vector(0, 8)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Keima(
	"c",
	SuperListGen.immutableOf(new Vector(1, 2),
	    new Vector(-1, 2)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Gin(
	"d",
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, -1),
	    new Vector(-1, -1)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Kin(
	"e",
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	SuperListGen.empty(),
	4),
    Kaku(
	"f",
	SuperListGen.immutableOf(new Vector(8, 8), new Vector(8, -8),
	    new Vector(-8, 8), new Vector(-8, -8)),
	SuperListGen.immutableOf(new Vector(8, 8), new Vector(8, -8),
	    new Vector(-8, 8), new Vector(-8, -8),
	    new Vector(0, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	2),
    Hisha(
	"g",
	SuperListGen.immutableOf(new Vector(0, 8), new Vector(0, -8),
	    new Vector(8, 0), new Vector(-8, 0)),
	SuperListGen.immutableOf(new Vector(0, 8), new Vector(0, -8),
	    new Vector(8, 0), new Vector(-8, 0),
	    new Vector(1, 1), new Vector(-1, 1),
	    new Vector(1, -1), new Vector(-1, -1)),
	2),
    Ou(
	"h",
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1),
	    new Vector(1, -1), new Vector(-1, -1)),
	SuperListGen.empty(),
	2),
    Empty(
	"*",
	SuperListGen.empty(),
	SuperListGen.empty(),
	81);

    private final String id;
    public final ImmutableSuperList<Vector> territory;
    public final ImmutableSuperList<Vector> nariTerri;
    public final ImmutableSuperList<Vector> territoryRev;
    public final ImmutableSuperList<Vector> nariTerriRev;
    public final int numLimit;

    private Koma(
	String id,
	ImmutableSuperList<Vector> territory,
	ImmutableSuperList<Vector> nariTerri,
	int numLimit) {
	
	this.id = id;
	this.territory = territory;
	this.nariTerri = nariTerri;
	this.territoryRev = SuperListGen.immutableOf(
	    territory.stream().map(v -> v.reverse(false, true)).toArray(Vector[]::new));
	this.nariTerriRev = SuperListGen.immutableOf(
	    nariTerri.stream().map(v -> v.reverse(false, true)).toArray(Vector[]::new));
	this.numLimit = numLimit;
    }

    public static Koma of(String id) {
	return Arrays.stream(Koma.values())
	    .filter(p -> StringUtils.equals(id, p.id))
	    .findFirst()
	    .orElse(null);
    }

    public boolean canNari() {
	return this != Ou && this != Kin && this != Empty;
    }
}
