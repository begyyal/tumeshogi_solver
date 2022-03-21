package begyyal.shogi.processor;

import begyyal.shogi.def.Koma;
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
	    out[i] = writeItte(result.log.get(i), i, before);
	    before = result.log.get(i);
	}
	return out;
    }

    private String writeItte(KihuRecord rec, int idx, KihuRecord before) {

	boolean sente = idx % 2 == 0;
	var sb = new StringBuilder();

	sb.append(sente ? "先手：" : "後手：");
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
}
