package begyyal.shogi.def;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList.ImmutableSuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;

public enum Koma {

    Hu(
	"a",
	SuperListGen.immutableOf(new Vector(0, 1)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Kyousha(
	"b",
	SuperListGen.immutableOf(new Vector(0, 8)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Keima(
	"c",
	SuperListGen.immutableOf(new Vector(1, 2),
		new Vector(-1, 2)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Gin(
	"d",
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, -1),
		new Vector(-1, -1)),
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Kin(
	"e",
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1)),
	null),
    Kaku(
	"f",
	SuperListGen.immutableOf(new Vector(8, 8), new Vector(8, -8),
		new Vector(-8, 8), new Vector(-8, -8)),
	SuperListGen.immutableOf(new Vector(8, 8), new Vector(8, -8),
		new Vector(-8, 8), new Vector(-8, -8),
		new Vector(0, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Hisha(
	"g",
	SuperListGen.immutableOf(new Vector(0, 8), new Vector(0, -8),
		new Vector(8, 0), new Vector(-8, 0)),
	SuperListGen.immutableOf(new Vector(0, 8), new Vector(0, -8),
		new Vector(8, 0), new Vector(-8, 0),
		new Vector(1, 1), new Vector(-1, 1),
		new Vector(1, -1), new Vector(-1, -1))),
    Ou(
	"h",
	SuperListGen.immutableOf(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1),
		new Vector(1, -1), new Vector(-1, -1)),
	null),
    Empty(
	"*",
	null,
	null);

    private final String id;
    public final ImmutableSuperList<Vector> territory;
    public final ImmutableSuperList<Vector> nariTerri;

    private Koma(
	    String id,
	    ImmutableSuperList<Vector> territory,
	    ImmutableSuperList<Vector> nariTerri) {
	this.id = id;
	this.territory = territory;
	this.nariTerri = nariTerri;
    }
    
    public static Koma of(String id) {
	return Arrays.stream(Koma.values())
		.filter(p -> StringUtils.equals(id, p.id))
		.findFirst()
		.orElse(null);
    }
}
