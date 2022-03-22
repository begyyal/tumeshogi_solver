package begyyal.shogi.def;

public enum KihuRel {
    Migi("m", "右"),
    Hidari("n", "左");

    public final String id;
    public final String desc;

    private KihuRel(String id, String desc) {
	this.id = id;
	this.desc = desc;
    }
}
