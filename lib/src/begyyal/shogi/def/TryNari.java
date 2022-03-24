package begyyal.shogi.def;

import begyyal.shogi.def.common.KihuOpt;

public enum TryNari {
    Rezu(null),
    Razu(KihuOpt.Narazu),
    Ru(KihuOpt.Nari);

    public final KihuOpt kihu;

    private TryNari(KihuOpt kihu) {
	this.kihu = kihu;
    }
}
