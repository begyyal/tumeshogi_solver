package begyyal.shogi.processor;

import java.util.stream.Stream;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.MasuState;

public abstract class PlayerProcessorBase {

    protected final int numOfMoves;

    protected PlayerProcessorBase(int numOfMoves) {
	this.numOfMoves = numOfMoves;
    }

    protected Stream<MasuState> spreadMasuState(MasuState from, Ban ban) {
	return ban.search(s -> s.rangedBy.contains(from.x, from.y))
	    .filter(s -> this.canAdvanceTo(s));
    }

    protected boolean canAdvanceTo(MasuState state) {
	return state != MasuState.Invalid
		&& (state.koma == Koma.Empty || state.player != getPlayerType());
    }

    // 成ってもいいし成らなくてもいい
    protected Stream<Boolean> createBranchStream(int y, MasuState from) {
	return !from.nariFlag
		&& (getPlayerType() == Player.Self ? y > 5 : y < 3)
		&& from.koma.canNari()
			? Stream.of(false, true)
			: Stream.of(false);
    }

    protected abstract Player getPlayerType();
}
