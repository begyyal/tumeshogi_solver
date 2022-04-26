package begyyal.shogi.def.common;

public enum KihuRel {
    Migi("m", "右"),
    Hidari("n", "左");

    public final String key;
    public final String desc;

    private KihuRel(String key, String desc) {
	this.key = key;
	this.desc = desc;
    }
}
