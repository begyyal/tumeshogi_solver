package begyyal.shogi.def.common;

public enum KihuAct {
    Agaru("p", "上"),
    Yoru("q", "寄"),
    Hiku("r", "引"),
    Sugu("s", "直");

    public final String key;
    public final String desc;

    private KihuAct(String key, String desc) {
	this.key = key;
	this.desc = desc;
    }
}
