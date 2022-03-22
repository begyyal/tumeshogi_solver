package begyyal.shogi.def;

import java.util.Arrays;

import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XList.ImmutableXList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.commons.util.function.XStrings;

public enum Koma {

    Hu(
	"a",
	"歩",
	"と",
	XListGen.immutableOf(new Vector(0, 1)),
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	18),
    Kyousha(
	"b",
	"香",
	"成香",
	XListGen.immutableOf(new Vector(0, 8)),
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Keima(
	"c",
	"桂",
	"成桂",
	XListGen.immutableOf(
	    new Vector(1, 2), new Vector(-1, 2)),
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4),
    Gin(
	"d",
	"銀",
	"成銀",
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
	"金",
	null,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	XListGen.empty(),
	4),
    Kaku(
	"f",
	"角",
	"馬",
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
	"飛",
	"龍",
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
	"王",
	"玉",
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1),
	    new Vector(1, -1), new Vector(-1, -1)),
	XListGen.empty(),
	2),
    Empty(
	"*",
	null,
	null,
	XListGen.empty(),
	XListGen.empty(),
	81);

    public final String id;
    public final String desc1;
    public final String desc2;
    public final ImmutableXList<Vector> territory;
    public final ImmutableXList<Vector> nariTerri;
    public final ImmutableXList<Vector> territoryRev;
    public final ImmutableXList<Vector> nariTerriRev;
    public final int numLimit;

    private Koma(
	String id,
	String desc1,
	String desc2,
	ImmutableXList<Vector> territory,
	ImmutableXList<Vector> nariTerri,
	int numLimit) {

	this.id = id;
	this.desc1 = desc1;
	this.desc2 = desc2;
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
