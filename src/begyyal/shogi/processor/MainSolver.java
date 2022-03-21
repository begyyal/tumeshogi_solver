package begyyal.shogi.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import begyyal.shogi.def.Koma;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.KihuRecord;

public class MainSolver implements Closeable {

    private final DerivationCalculator dc;

    public MainSolver(Args args) {
	this.dc = new DerivationCalculator(
	    args.numOfMoves,
	    args.initBan,
	    args.motigoma);
    }

    public String[] calculate() throws InterruptedException, ExecutionException {
	var result = this.dc.ignite();
	return result == null
		? createFailureLabel()
		: this.summarize(result);
    }

    private String[] createFailureLabel() {
	return new String[] { "詰めませんでした。" };
    }

    private String[] summarize(BanContext result) {
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

    @Override
    public void close() throws IOException {
	this.dc.close();
    }
}
