package begyyal.shogi.object;

import begyyal.shogi.def.KihuAct;
import begyyal.shogi.def.KihuOpt;
import begyyal.shogi.def.KihuRel;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.def.TryNari;

public class KihuRecord {

    public final int fsuzi;
    public final int fdan;
    public final int suzi;
    public final int dan;
    public final Koma koma;
    public final boolean nari;
    public final KihuRel rel;
    public final KihuAct act;
    public final KihuOpt opt;
    public final boolean deploy; // 打歩詰め判定のみで利用
    public final int hash;

    private KihuRecord(
	int fx, int fy,
	int x, int y,
	Koma koma,
	boolean nari,
	KihuRel rel,
	KihuAct act,
	KihuOpt opt,
	boolean deploy) {

	this.fsuzi = 9 - fx;
	this.fdan = 9 - fy;
	this.suzi = 9 - x;
	this.dan = 9 - y;
	this.koma = koma;
	this.nari = nari;
	this.rel = rel;
	this.act = act;
	this.opt = opt;
	this.deploy = deploy;
	this.hash = (((((9 + x)
		* 9 + y)
		* 9 + koma.ordinal())
		* 3 + (rel == null ? 2 : rel.ordinal()))
		* 5 + (act == null ? 4 : act.ordinal()))
		* 5 + (opt == null ? 4 : opt.ordinal());
    }

    public static KihuRecord resolveAdvance(
	MasuState s,
	Player player,
	Koma koma,
	boolean nari,
	int fx,
	int fy,
	TryNari tn) {

	int tx = s.ss.x, ty = s.ss.y;
	if (!s.rangedBy.stream()
	    .anyMatch(ss -> ss.koma == koma
		    && ss.nari == nari
		    && ss.player == player
		    && (ss.x != fx || ss.y != fy)))
	    return new KihuRecord(fx, fy, tx, ty, koma, nari, null, null, tn.kihu, false);

	if (fy - s.ss.y < 0 && fx == s.ss.x && koma != Koma.Hisha && koma != Koma.Kaku)
	    return new KihuRecord(fx, fy, tx, ty, koma, nari, null, KihuAct.Sugu, tn.kihu, false);

	var ite = s.rangedBy.stream()
	    .filter(ss -> ss.koma == koma
		    && ss.nari == nari
		    && ss.player == player
		    && (ss.x != fx || ss.y != fy))
	    .iterator();

	KihuAct act = null;
	KihuRel rel = null;
	boolean vdup = false;
	while (ite.hasNext()) {
	    var ss = ite.next();
	    if (ss.x == fx)
		vdup = true;
	    else if (ss.y == fy)
		rel = ss.x - fx < 0 ? KihuRel.Migi : KihuRel.Hidari;
	}

	if (vdup)
	    act = fy - ty < 0 ? KihuAct.Agaru : fy - ty > 0 ? KihuAct.Hiku : KihuAct.Yoru;

	return new KihuRecord(fx, fy, tx, ty, koma, nari, rel, act, tn.kihu, false);
    }

    public static KihuRecord resolveDeploy(
	MasuState s,
	Player player,
	Koma koma) {
	int tx = s.ss.x, ty = s.ss.y;
	return s.rangedBy.stream()
	    .anyMatch(ss -> ss.koma == koma && ss.player == player && !ss.nari)
		    ? new KihuRecord(-1, -1, tx, ty, koma, false, null, null, KihuOpt.Utu, true)
		    : new KihuRecord(-1, -1, tx, ty, koma, false, null, null, null, true);
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof KihuRecord))
	    return false;
	return this.hash == ((KihuRecord) o).hash;
    }

    @Override
    public int hashCode() {
	return this.hash;
    }
}
