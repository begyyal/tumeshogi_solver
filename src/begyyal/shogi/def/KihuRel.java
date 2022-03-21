package begyyal.shogi.def;

public enum KihuRel {
    Migi("右"),
    Hidari("左");

    public final String desc;

    private KihuRel(String desc) {
	this.desc = desc;
    }
}
