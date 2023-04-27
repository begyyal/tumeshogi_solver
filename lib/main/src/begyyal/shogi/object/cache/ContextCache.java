package begyyal.shogi.object.cache;

import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.KihuRecord;
import begyyal.shogi.object.MotigomaState;

public class ContextCache {

    public static final ContextCache dummy = new ContextCache(null, null, null);

    public final XList<KihuRecord> log;
    public final MotigomaState[] motigoma;
    public final Ban ban;

    public ContextCache(
	XList<KihuRecord> log,
	MotigomaState[] motigoma,
	Ban ban) {
	this.log = log;
	this.motigoma = motigoma;
	this.ban = ban;
    }

    public BanContext restoreContext(XList<KihuRecord> ancestor) {
	var log = XListGen.of(ancestor);
	log.addAll(this.log);
	return new BanContext(log, this.ban, this.motigoma);
    }
}
