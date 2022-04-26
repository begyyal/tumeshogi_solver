package begyyal.shogi.def;

import java.util.Arrays;

import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XList.ImmutableXList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.commons.util.function.XStrings;
import begyyal.shogi.def.common.TsKoma;

public enum Koma {

    Hu(
	"a",
	false,
	XListGen.immutableOf(new Vector(0, 1)),
	18,
	0),
    Tokin(
	"a",
	true,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	18,
	0),
    Kyousya(
	"b",
	false,
	XListGen.immutableOf(new Vector(0, 8)),
	4,
	1),
    NariKyou(
	"b",
	true,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4,
	1),
    Keima(
	"c",
	false,
	XListGen.immutableOf(
	    new Vector(1, 2), new Vector(-1, 2)),
	4,
	2),
    NariKei(
	"c",
	true,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4,
	2),
    Gin(
	"d",
	false,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, -1),
	    new Vector(-1, -1)),
	4,
	3),
    NariGin(
	"d",
	true,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4,
	3),
    Kin(
	"e",
	false,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	4,
	4),
    Kaku(
	"f",
	false,
	XListGen.immutableOf(
	    new Vector(8, 8), new Vector(8, -8),
	    new Vector(-8, 8), new Vector(-8, -8)),
	2,
	5),
    Uma(
	"f",
	true,
	XListGen.immutableOf(
	    new Vector(8, 8), new Vector(8, -8),
	    new Vector(-8, 8), new Vector(-8, -8),
	    new Vector(0, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1)),
	2,
	5),
    Hisya(
	"g",
	false,
	XListGen.immutableOf(
	    new Vector(0, 8), new Vector(0, -8),
	    new Vector(8, 0), new Vector(-8, 0)),
	2,
	6),
    Ryuu(
	"g",
	true,
	XListGen.immutableOf(
	    new Vector(0, 8), new Vector(0, -8),
	    new Vector(8, 0), new Vector(-8, 0),
	    new Vector(1, 1), new Vector(-1, 1),
	    new Vector(1, -1), new Vector(-1, -1)),
	2,
	6),
    Ou(
	"h",
	false,
	XListGen.immutableOf(
	    new Vector(0, 1), new Vector(1, 1),
	    new Vector(-1, 1), new Vector(1, 0),
	    new Vector(-1, 0), new Vector(0, -1),
	    new Vector(1, -1), new Vector(-1, -1)),
	2,
	-1),
    Empty(
	"*",
	false,
	XListGen.empty(),
	81,
	-1);

    public final String key;
    public final boolean nari;
    public final ImmutableXList<Vector> territory;
    public final ImmutableXList<Vector> territoryRev;
    public final int numLimit;
    public final int midx;

    private Koma(
	String key,
	boolean nari,
	ImmutableXList<Vector> territory,
	int numLimit,
	int midx) {

	this.key = key;
	this.nari = nari;
	this.territory = territory;
	this.territoryRev = XListGen.immutableOf(
	    territory.stream().map(v -> v.reverse(false, true)).toArray(Vector[]::new));
	this.numLimit = numLimit;
	this.midx = midx;
    }

    public static Koma of(TsKoma koma) {
	return of(koma.key, koma.nari);
    }

    public static Koma of(String key, boolean nari) {
	return Arrays.stream(Koma.values())
	    .filter(p -> XStrings.equals(key, p.key) && p.nari == nari)
	    .findFirst()
	    .orElse(null);
    }

    public boolean canNari() {
	return !this.nari && this != Ou && this != Kin && this != Empty;
    }

    public Koma naru() {
	return Arrays.stream(Koma.values())
	    .filter(p -> XStrings.equals(key, p.key) && p.nari)
	    .findFirst()
	    .orElse(null);
    }
}
