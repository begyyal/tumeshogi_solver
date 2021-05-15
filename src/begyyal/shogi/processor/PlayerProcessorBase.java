package begyyal.shogi.processor;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.MasuState;

public abstract class PlayerProcessorBase {

    protected PlayerProcessorBase() {
    }

    protected Stream<MasuState> spreadMasuState(MasuState from, Ban ban) {
	return ban.search(s -> s.rangedBy.contains(from.x, from.y))
	    .filter(s -> this.canAdvanceTo(s));
    }

    protected boolean canAdvanceTo(MasuState state) {
	return state != MasuState.Invalid
		&& (state.koma == Koma.Empty || state.player != getPlayerType());
    }

    protected boolean isOpponentOu(MasuState s) {
	return s.player == Player.Opponent && s.koma == Koma.Ou;
    }

    protected IntStream createBranchStream(int y, Koma koma) {
	return IntStream.range(0,
	    (getPlayerType() == Player.Self ? y > 5 : y < 3) && koma.canNari() ? 2 : 1);
    }

    protected abstract Player getPlayerType();
}
