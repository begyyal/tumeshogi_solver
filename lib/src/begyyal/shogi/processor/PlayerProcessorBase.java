package begyyal.shogi.processor;

import java.util.stream.Stream;

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
	return state != MasuState.Invalid
		&& (state.ss.koma == Koma.Empty || state.ss.player != getPlayerType());
    }

    protected Stream<TryNari> createBranchStream(int y, SmartMasuState from) {
	return !from.koma.nari
		&& (getPlayerType() == Player.Sente ? y > 5 : y < 3)
		&& from.koma.canNari()
			? Stream.of(TryNari.Razu, TryNari.Ru)
			: Stream.of(TryNari.Rezu);
    }

    protected abstract Player getPlayerType();
}
