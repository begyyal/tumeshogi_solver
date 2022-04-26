package begyyal.shogi.entity;

import begyyal.shogi.def.common.KihuAct;
import begyyal.shogi.def.common.KihuOpt;
import begyyal.shogi.def.common.KihuRel;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;

public class TsKihuRecord {

    /** 先手/後手 */
    public final Player player;
    /** 移動元の筋(持駒からの場合は-1) */
    public final int fromSuzi;
    /** 移動元の段(持駒からの場合は-1) */
    public final int fromDan;
    
    /** 筋 */
    public final int suzi;
    /** 段 */
    public final int dan;
    /** 「同」フラグ */
    public final boolean dou;
    /**　駒種別(成駒含む/玉含まず/14種) */
    public final TsKoma koma;
    /** 相対位置(右/左)　*/
    public final KihuRel rel;
    /** 動作(上/寄/引/直) */
    public final KihuAct act;
    /** その他(成/不成/打) */
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

    /** 全角棋譜表記 */
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
