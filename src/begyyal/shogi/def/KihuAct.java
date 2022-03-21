package begyyal.shogi.def;

public enum KihuAct {
    Agaru("上"),
    Yoru("寄"),
    Hiku("引"),
    Sugu("直");

    public final String desc;

    private KihuAct(String desc) {
	this.desc = desc;
    }
}
