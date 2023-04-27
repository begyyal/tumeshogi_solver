package begyyal.shogi.object.cache;

import begyyal.commons.object.collection.XList;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.KihuRecord;
import begyyal.shogi.object.MotigomaState;

public class ContextCache {

    public final XList<KihuRecord> log;
    public final MotigomaState[] motigoma;
    public final Ban ban;
    public final boolean success;
    public final int depth;

    public ContextCache(
	XList<KihuRecord> log,
	MotigomaState[] motigoma,
	Ban ban,
	int depth) {
	this(log, motigoma, ban, true, depth);
    }

    private ContextCache(
	XList<KihuRecord> log,
	MotigomaState[] motigoma,
	Ban ban,
	boolean success,
	int depth) {
	this.log = log;
	this.motigoma = motigoma;
	this.ban = ban;
	this.success = success;
	this.depth = depth;
    }

    public BanContext restoreContext(XList<KihuRecord> ancestor) {
	var log = XListGen.of(ancestor);
	log.addAll(this.log);
	return new BanContext(log, this.ban, this.motigoma);
    }

    public static ContextCache createFailure(int depth) {
	return new ContextCache(null, null, null, false, depth);
    }
}
