package begyyal.shogi.processor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import begyyal.commons.object.collection.XGen;
import begyyal.commons.util.cache.SimpleCacheResolver;
import begyyal.commons.util.function.XUtils;
import begyyal.shogi.constant.PublicCacheMapId;
import begyyal.shogi.def.Koma;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.cache.ContextCache;

public class DerivationCalculator {

    private final int numOfMoves;
    private final BanContext origin;
    private final SenteProcessor selfProcessor;
    private final GoteProcessor opponentProcessor;
    private final ExecutorService exe;

    public DerivationCalculator(Args args, ExecutorService exe) {
	this.numOfMoves = args.numOfMoves;
	this.origin = new BanContext(args.initBan, args.motigoma);
	this.selfProcessor = new SenteProcessor(numOfMoves);
	this.opponentProcessor = new GoteProcessor(numOfMoves);
	this.exe = exe;
    }

    public BanContext ignite() throws InterruptedException, ExecutionException {
	return this.r4spread(origin, this.selfProcessor.spread(origin), 1);
    }

    private BanContext r4spread(BanContext context, BanContext[] branches, int count)
	throws InterruptedException, ExecutionException {

	if (branches == null || branches.length == 0) {
	    if (count % 2 == 0) {
		var r = context.getLatestRecord();
		return r.koma == Koma.Hu && r.deploy ? null : context;
	    } else
		return null;
	} else if (count > numOfMoves)
	    return null;

	var futureMap = XGen.<BanContext, Future<BanContext[]>>newHashMap();
	for (var b : branches)
	    futureMap.put(b, this.exe.submit(count % 2 == 0
		    ? () -> this.selfProcessor.spread(b)
		    : () -> this.opponentProcessor.spread(b)));

	BanContext result = null;
	int depth = 0;
	var i = futureMap.entrySet().stream()
	    .sorted((e1, e2) -> XUtils.compare(e1.getKey(), e2.getKey()))
	    .iterator();

	while (i.hasNext()) {

	    var e = i.next();
	    var k = e.getKey();
	    var ck = k.generateCasheKey();

	    BanContext selected = null;
	    ContextCache cache = SimpleCacheResolver.getAsPublic(PublicCacheMapId.context, ck);
	    if (cache == null) {
		selected = r4spread(k, e.getValue().get(), count + 1);
		cache = selected == null ? ContextCache.dummy : selected.createCache(count);
		SimpleCacheResolver.putAsPublic(PublicCacheMapId.context, ck, cache);
	    } else if (cache != ContextCache.dummy)
		selected = cache.restoreContext(k.log);

	    if (count % 2 != 0) {
		if (selected != null && (result == null || result.log.size() > selected.log.size()))
		    result = selected;
	    } else if (selected == null) {
		return null;
	    } else if (depth < (depth = selected.log.size()))
		result = selected;
	}

	return result;
    }
}
