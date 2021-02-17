package begyyal.shogi.def;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;

public enum Koma {

    Hu(
	"a",
	SuperListGen.of(new Vector(0, 1)),
	SuperListGen.of(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Kyousha(
	"b",
	SuperListGen.of(new Vector(0, 8)),
	SuperListGen.of(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Keima(
	"c",
	SuperListGen.of(new Vector(1, 2),
		new Vector(-1, 2)),
	SuperListGen.of(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Gin(
	"d",
	SuperListGen.of(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, -1),
		new Vector(-1, -1)),
	SuperListGen.of(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Kin(
	"e",
	SuperListGen.of(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1)),
	null),
    Kaku(
	"f",
	SuperListGen.of(new Vector(8, 8), new Vector(8, -8),
		new Vector(-8, 8), new Vector(-8, -8)),
	SuperListGen.of(new Vector(8, 8), new Vector(8, -8),
		new Vector(-8, 8), new Vector(-8, -8),
		new Vector(0, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1))),
    Hisha(
	"g",
	SuperListGen.of(new Vector(0, 8), new Vector(0, -8),
		new Vector(8, 0), new Vector(-8, 0)),
	SuperListGen.of(new Vector(0, 8), new Vector(0, -8),
		new Vector(8, 0), new Vector(-8, 0),
		new Vector(1, 1), new Vector(-1, 1),
		new Vector(1, -1), new Vector(-1, -1))),
    Ou(
	"h",
	SuperListGen.of(new Vector(0, 1), new Vector(1, 1),
		new Vector(-1, 1), new Vector(1, 0),
		new Vector(-1, 0), new Vector(0, -1),
		new Vector(1, -1), new Vector(-1, -1)),
	null);

    private final String id;
    private final SuperList<Vector> territory;
    private final SuperList<Vector> nariTerri;

    private Koma(
	    String id,
	    SuperList<Vector> territory,
	    SuperList<Vector> nariTerri) {
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
