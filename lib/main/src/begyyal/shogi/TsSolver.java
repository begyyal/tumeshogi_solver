package begyyal.shogi;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import begyyal.commons.object.collection.XGen;
import begyyal.commons.object.collection.XList;
import begyyal.commons.util.cache.SimpleCacheResolver;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;
import begyyal.shogi.entity.TsKihuRecord;
import begyyal.shogi.entity.TsMasuState;
import begyyal.shogi.entity.TsMotigomaState;
import begyyal.shogi.object.KihuRecord;
import begyyal.shogi.processor.ArgConverter;
import begyyal.shogi.processor.ArgsValidator;
import begyyal.shogi.processor.DerivationCalculator;

public class TsSolver implements Closeable {

    private final ExecutorService exe;

    public TsSolver() {
	this.exe = Executors.newCachedThreadPool();
    }

    public List<TsKihuRecord> calculate(
	int numOfMoves,
	Set<TsMasuState> ban,
	Set<TsMotigomaState> motigoma)
	throws InterruptedException, ExecutionException {
	var context = this.preProcess(numOfMoves, ban, motigoma).ignite();
	return context == null ? Collections.emptyList() : convertKihu(context.log);
    }

    private DerivationCalculator preProcess(
	int numOfMoves,
	Set<TsMasuState> ban,
	Set<TsMotigomaState> motigoma) {
	var args = new ArgConverter().exe(numOfMoves, ban, motigoma);
	new ArgsValidator().validate(args);
	return new DerivationCalculator(args, exe);
    }

    private List<TsKihuRecord> convertKihu(XList<KihuRecord> kihu) {
	var result = XGen.<TsKihuRecord>newArrayList();
	KihuRecord before = null;
	for (int i = 0; i < kihu.size(); i++) {
	    var rec = kihu.get(i);
	    result.add(new TsKihuRecord(
		i % 2 == 0 ? Player.Sente : Player.Gote,
		rec.fsuzi > 9 ? -1 : rec.fsuzi,
		rec.fdan > 9 ? -1 : rec.fdan,
		rec.suzi, rec.dan,
		before != null && before.suzi == rec.suzi && before.dan == rec.dan,
		TsKoma.of(rec.koma.key, rec.koma.nari),
		rec.rel,
		rec.act,
		rec.opt));
	    before = rec;
	}
	return result;
    }

    @Override
    public void close() throws IOException {
	this.exe.shutdown();
	SimpleCacheResolver.clearAll();
    }
}
