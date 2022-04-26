package begyyal.shogi.def.common;

import java.util.Arrays;

import begyyal.commons.util.function.XStrings;

public enum TsKoma {

    Hu("a", "歩", false),
    Tokin("a", "と", true),
    Kyousya("b", "香", false),
    NariKyou("b", "成香", true),
    Keima("c", "桂", false),
    NariKei("c", "成桂", true),
    Gin("d", "銀", false),
    NariGin("d", "成銀", true),
    Kin("e", "金", false),
    Kaku("f", "角", false),
    Uma("f", "馬", true),
    Hisya("g", "飛", false),
    Ryuu("g", "龍", true),
    Ou("h", "王", false);

    public final String key;
    public final String desc;
    public final boolean nari;

    private TsKoma(String key, String desc, boolean nari) {
	this.key = key;
	this.desc = desc;
	this.nari = nari;
    }

    public static TsKoma of(String id, boolean nari) {
	return Arrays.stream(TsKoma.values())
	    .filter(p -> XStrings.equals(id, p.key) && p.nari == nari)
	    .findFirst()
	    .orElse(null);
    }

}
