package begyyal.shogi.def;

import java.util.Arrays;

import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XList.ImmutableXList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.commons.util.function.XStrings;

public enum Koma {

    Hu(
	"a",
	XListGen.immutableOf(new Vector(0, 1)),
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	18),
    Kyousha(
	"b",
	XListGen.immutableOf(new Vector(0, 8)),
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Keima(
	"c",
	XListGen.immutableOf(
	    new Vector(1, 2), new Vector(-1, 2)),
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Gin(
	"d",
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, -1),
	    new Vector(-1, -1)),
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Kin(
	"e",
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	XListGen.empty(),
	4),
    Kaku(
	"f",
	XListGen.immutableOf(
	    new Vector(8, 8), new Vector(8, -8),
	    new Vector(-8, 8), new Vector(-8, -8)),
	XListGen.immutableOf(
	    new Vector(8, 8), new Vector(8, -8),
	    new Vector(-8, 8), new Vector(-8, -8),
	    new Vector(0, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	2),
    Hisha(
	"g",
	XListGen.immutableOf(
	    new Vector(0, 8), new Vector(0, -8),
	    new Vector(8, 0), new Vector(-8, 0)),
	XListGen.immutableOf(
	    new Vector(0, 8), new Vector(0, -8),
	    new Vector(8, 0), new Vector(-8, 0),
	    new Vector(1, 1), new Vector(-1, 1),
	    new Vector(1, -1), new Vector(-1, -1)),
	2),
    Ou(
	"h",
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1),
	    new Vector(1, -1), new Vector(-1, -1)),
	XListGen.empty(),
	2),
    Empty(
	"*",
	XListGen.empty(),
	XListGen.empty(),
	81);

    private final String id;
    public final ImmutableXList<Vector> territory;
    public final ImmutableXList<Vector> nariTerri;
    public final ImmutableXList<Vector> territoryRev;
    public final ImmutableXList<Vector> nariTerriRev;
    public final int numLimit;

    private Koma(
	String id,
	ImmutableXList<Vector> territory,
	ImmutableXList<Vector> nariTerri,
	int numLimit) {

	this.id = id;
	this.territory = territory;
	this.nariTerri = nariTerri;
	this.territoryRev = XListGen.immutableOf(
	    territory.stream().map(v -> v.reverse(false, true)).toArray(Vector[]::new));
	this.nariTerriRev = XListGen.immutableOf(
	    nariTerri.stream().map(v -> v.reverse(false, true)).toArray(Vector[]::new));
	this.numLimit = numLimit;
    }

    public static Koma of(String id) {
	return Arrays.stream(Koma.values())
	    .filter(p -> XStrings.equals(id, p.id))
	    .findFirst()
	    .orElse(null);
    }

    public boolean canNari() {
	return this != Ou && this != Kin && this != Empty;
    }
}
