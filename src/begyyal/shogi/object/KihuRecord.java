package begyyal.shogi.object;

import begyyal.shogi.def.KihuAct;
import begyyal.shogi.def.KihuOpt;
import begyyal.shogi.def.KihuRel;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.def.TryNari;

public class KihuRecord {

    public final int suzi;
    public final int dan;
    public final Koma koma;
    public final boolean nari;
    public final KihuRel rel;
    public final KihuAct act;
    public final KihuOpt opt;
    public final boolean deploy;
    public final int hash;

    private KihuRecord(
	int x, int y,
	Koma koma,
	boolean nari,
	KihuRel rel,
	KihuAct act,
	KihuOpt opt,
	boolean deploy) {

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
	int fromX,
	int fromY,
	TryNari tn) {

	if (!s.rangedBy.stream()
	    .anyMatch(ss -> ss.koma == koma
		    && ss.nari == nari
		    && ss.player == player
		    && (ss.x != fromX || ss.y != fromY)))
	    return new KihuRecord(s.ss.x, s.ss.y, koma, nari, null, null, tn.kihu, false);

	if (fromY - s.ss.y < 0 && fromX == s.ss.x && koma != Koma.Hisha && koma != Koma.Kaku)
	    return new KihuRecord(s.ss.x, s.ss.y, koma, nari, null, KihuAct.Sugu, tn.kihu, false);

	var ite = s.rangedBy.stream()
	    .filter(ss -> ss.koma == koma
		    && ss.nari == nari
		    && ss.player == player
		    && (ss.x != fromX || ss.y != fromY))
	    .iterator();

	KihuAct act = null;
	KihuRel rel = null;
	boolean vdup = false;
	while (ite.hasNext()) {
	    var ss = ite.next();
	    if (ss.x == fromX)
		vdup = true;
	    else if (ss.y == fromY)
		rel = ss.x - fromX < 0 ? KihuRel.Migi : KihuRel.Hidari;
	}

	if (vdup)
	    act = fromY - s.ss.y < 0 ? KihuAct.Agaru
		    : fromY - s.ss.y > 0 ? KihuAct.Hiku : KihuAct.Yoru;

	return new KihuRecord(s.ss.x, s.ss.y, koma, nari, rel, act, tn.kihu, false);
    }

    public static KihuRecord resolveDeploy(
	MasuState s,
	Player player,
	Koma koma) {
	return s.rangedBy.stream()
	    .anyMatch(ss -> ss.koma == koma && ss.player == player && !ss.nari)
		    ? new KihuRecord(s.ss.x, s.ss.y, koma, false, null, null, KihuOpt.Utu, true)
		    : new KihuRecord(s.ss.x, s.ss.y, koma, false, null, null, null, true);
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
