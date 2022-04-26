package begyyal.shogi.cmd.processor;

import java.util.List;

import begyyal.commons.constant.Strs;
import begyyal.shogi.entity.TsKihuRecord;

public class Summarizer {

    private final boolean translate;

    public Summarizer(boolean translate) {
	this.translate = translate;
    }

    public String[] exe(List<TsKihuRecord> kihu) {
	if (kihu.size() == 0)
	    return this.translate
		    ? new String[] { "詰めませんでした。" }
		    : new String[] {};
	return kihu.stream()
	    .map(kr -> this.translate
		    ? this.writeItteAsT(kr)
		    : this.writeItteAsI(kr))
	    .toArray(String[]::new);
    }

    private String writeItteAsT(TsKihuRecord rec) {
	return rec.player.desc + "：" + rec.toString();
    }

    private String writeItteAsI(TsKihuRecord rec) {

	var sb = new StringBuilder();

	sb.append(rec.player.key);

	if (rec.fromSuzi != -1) {
	    sb.append(rec.fromSuzi);
	    sb.append(rec.fromDan);
	} else
	    sb.append(Strs.hyphen);

	sb.append(rec.suzi);
	sb.append(rec.dan);
	sb.append(rec.dou ? "j" : Strs.hyphen);

	sb.append(rec.koma.key);
	sb.append(rec.koma.nari ? "z" : Strs.hyphen);

	sb.append(rec.rel == null ? Strs.hyphen : rec.rel.key);
	sb.append(rec.act == null ? Strs.hyphen : rec.act.key);
	sb.append(rec.opt == null ? Strs.hyphen : rec.opt.key);

	return sb.toString();
    }
}
