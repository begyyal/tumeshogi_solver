package begyyal.shogi.def;

public enum KihuAct {
    Agaru("p", "上"),
    Yoru("q", "寄"),
    Hiku("r", "引"),
    Sugu("s", "直");

    public final String id;
    public final String desc;

    private KihuAct(String id, String desc) {
	this.id = id;
	this.desc = desc;
    }
}
