package begyyal.shogi.def;

public enum KihuOpt {
    Nari("t", "成"),
    Narazu("u", "不成"),
    Utu("v", "打");

    public final String id;
    public final String desc;

    private KihuOpt(String id, String desc) {
	this.id = id;
	this.desc = desc;
    }

    public static KihuOpt of(boolean nari) {
	return nari ? KihuOpt.Nari : KihuOpt.Narazu;
    }
}
