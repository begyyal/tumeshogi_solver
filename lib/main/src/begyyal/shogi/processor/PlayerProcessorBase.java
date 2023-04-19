package begyyal.shogi.processor;

import java.util.Set;
import java.util.stream.Stream;

import begyyal.commons.object.Vector;
import begyyal.commons.object.collection.XGen;
import begyyal.commons.util.cache.SimpleCacheResolver;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.TryNari;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.MasuState;
import begyyal.shogi.object.MasuState.SmartMasuState;

public abstract class PlayerProcessorBase {

    protected final int numOfMoves;

    protected PlayerProcessorBase(int numOfMoves) {
	this.numOfMoves = numOfMoves;
    }

    protected Stream<MasuState> spreadMasuState(MasuState from, Ban ban) {
	return ban.search(s -> s.rangedBy.contains(from.ss)).filter(s -> this.canAdvanceTo(s));
    }

    protected boolean canAdvanceTo(MasuState state) {
	return state != null && (state.ss.koma == Koma.Empty || state.ss.player != getPlayerType());
    }

    private boolean isNariArea(int y, Player ply) {
	return ply == Player.Sente ? y > 5 : y < 3;
    }

    protected Stream<TryNari> createBranchStream(int y, SmartMasuState from) {
	return this.createBranchStream(y, from, this.getPlayerType());
    }

    protected Stream<TryNari> createBranchStream(int y, SmartMasuState from, Player ply) {
	return !from.koma.nari
		&& (this.isNariArea(y, ply) || this.isNariArea(from.y, ply))
		&& from.koma.canNari()
			? Stream.of(TryNari.Razu, TryNari.Ru)
			: Stream.of(TryNari.Rezu);
    }

    protected Set<Vector> getTerritoryAfterMoved(int y, SmartMasuState from) {
	return this.getTerritoryAfterMoved(y, from, getPlayerType());
    }

    protected Set<Vector> getTerritoryAfterMoved(int y, SmartMasuState from, Player ply) {
	int key = ((9 + y) * 16 + from.koma.ordinal()) * 2 + ply.ordinal();
	return SimpleCacheResolver.getAsPrivate(PlayerProcessorBase.class, 1, key, () -> {
	    var dt = XGen.newHashSet(MasuState.getDecomposedTerritory(from.koma, ply));
	    if ((this.isNariArea(y, ply) || this.isNariArea(from.y, ply)) && from.koma.canNari())
		dt.addAll(MasuState.getDecomposedTerritory(from.koma.naru(), ply));
	    return dt;
	});
    }

    protected abstract Player getPlayerType();
}
