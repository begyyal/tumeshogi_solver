package begyyal.shogi.processor;

import java.util.stream.IntStream;

import begyyal.commons.constant.Strs;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.KihuRecord;

public class Summarizer {

    private final boolean translate;

    public Summarizer(boolean translate) {
	this.translate = translate;
    }

    public String[] exe(BanContext result) {
	var out = new String[result.log.size()];
	KihuRecord before = null;
	for (int i = 0; i < out.length; i++) {
	    out[i] = this.translate
		    ? this.writeItteAsT(result.log.get(i), i, before)
		    : this.writeItteAsI(result.log.get(i), i, before);
	    before = result.log.get(i);
	}
	return out;
    }

    public String[] createFailure() {
	return this.translate ? new String[] { "詰めませんでした。" } : new String[] {};
    }

    private String writeItteAsT(KihuRecord rec, int idx, KihuRecord before) {

	boolean sente = idx % 2 == 0;
	var sb = new StringBuilder();

	sb.append(sente ? Player.Self.desc : Player.Opponent.desc);
	sb.append("：");

	if (before != null && before.suzi == rec.suzi && before.dan == rec.dan) {
	    sb.append("同");
	} else {
	    sb.append((char) (String.valueOf(rec.suzi).charAt(0) + 0xFEE0));
	    sb.append((char) (String.valueOf(rec.dan).charAt(0) + 0xFEE0));
	}

	if (rec.koma == Koma.Ou)
	    sb.append(sente ? rec.koma.desc1 : rec.koma.desc2);
	else
	    sb.append(rec.nari ? rec.koma.desc2 : rec.koma.desc1);
	if (rec.rel != null)
	    sb.append(rec.rel.desc);
	if (rec.act != null)
	    sb.append(rec.act.desc);
	if (rec.opt != null)
	    sb.append(rec.opt.desc);

	return sb.toString();
    }

    private String writeItteAsI(KihuRecord rec, int idx, KihuRecord before) {

	var sb = new StringBuilder();

	sb.append(idx % 2 == 0 ? Player.Self.id : Player.Opponent.id);

	if (rec.fsuzi != 10) {
	    sb.append(rec.fsuzi);
	    sb.append(rec.fdan);
	} else
	    sb.append(Strs.hyphen);

	sb.append(rec.suzi);
	sb.append(rec.dan);

	if (before != null && before.suzi == rec.suzi && before.dan == rec.dan)
	    sb.append("j");
	else
	    sb.append(Strs.hyphen);

	sb.append(rec.koma.id);
	sb.append(rec.nari ? "z" : Strs.hyphen);

	sb.append(rec.rel == null ? Strs.hyphen : rec.rel.id);
	sb.append(rec.act == null ? Strs.hyphen : rec.act.id);
	sb.append(rec.opt == null ? Strs.hyphen : rec.opt.id);

	return sb.toString();
    }
}
