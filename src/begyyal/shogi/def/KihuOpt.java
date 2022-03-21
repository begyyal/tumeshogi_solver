package begyyal.shogi.def;

public enum KihuOpt {
    Nari("成"),
    Narazu("不成"),
    Utu("打");

    public final String desc;

    private KihuOpt(String desc) {
	this.desc = desc;
    }

    public static KihuOpt of(boolean nari) {
	return nari ? KihuOpt.Nari : KihuOpt.Narazu;
    }
}
