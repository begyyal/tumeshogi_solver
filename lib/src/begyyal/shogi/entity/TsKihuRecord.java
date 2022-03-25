package begyyal.shogi.entity;

import begyyal.shogi.def.common.KihuAct;
import begyyal.shogi.def.common.KihuOpt;
import begyyal.shogi.def.common.KihuRel;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;

public class TsKihuRecord {

    public final Player player;
    public final int fromSuzi;
    public final int fromDan;
    public final int suzi;
    public final int dan;
    public final boolean dou;
    public final TsKoma koma;
    public final KihuRel rel;
    public final KihuAct act;
    public final KihuOpt opt;

    public TsKihuRecord(
	Player player,
	int fromSuzi, int fromDan,
	int suzi, int dan,
	boolean dou,
	TsKoma koma,
	KihuRel rel,
	KihuAct act,
	KihuOpt opt) {

	this.player = player;
	this.fromSuzi = fromSuzi;
	this.fromDan = fromDan;
	this.suzi = suzi;
	this.dan = dan;
	this.dou = dou;
	this.koma = koma;
	this.rel = rel;
	this.act = act;
	this.opt = opt;
    }

    @Override
    public String toString() {
	var sb = new StringBuilder();
	if (this.dou) {
	    sb.append("同");
	} else {
	    sb.append((char) (String.valueOf(suzi).charAt(0) + 0xFEE0));
	    sb.append((char) (String.valueOf(dan).charAt(0) + 0xFEE0));
	}
	sb.append(this.koma == TsKoma.Ou && this.player == Player.Gote ? "玉" : this.koma.desc);
	if (this.rel != null)
	    sb.append(this.rel.desc);
	if (this.act != null)
	    sb.append(this.act.desc);
	if (this.opt != null)
	    sb.append(this.opt.desc);
	return sb.toString();
    }
}
