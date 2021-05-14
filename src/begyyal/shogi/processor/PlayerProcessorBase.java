package begyyal.shogi.processor;

import java.util.stream.Stream;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.MasuState;

public abstract class PlayerProcessorBase {

    protected PlayerProcessorBase() {
    }

    protected Stream<MasuState> spreadMasuState(MasuState from, Ban ban) {
	return ban.search(s -> s.rangedBy.contains(from))
	    .filter(s -> this.canAdvanceTo(s))
	    .map(s -> this.occupy(from, s))
	    .filter(s -> s != null);
    }

    protected MasuState occupy(MasuState from, MasuState to) {
	return from.player == to.player ? null
		: new MasuState(
		    from.player,
		    from.koma,
		    to.x,
		    to.y,
		    from.nariFlag || to.y <= 3,
		    to.rangedBy);
    }

    protected boolean canAdvanceTo(MasuState state) {
	return state != MasuState.Invalid
		&& (state.koma == Koma.Empty || state.player != getPlayerType());
    }

    protected boolean isOpponentOu(MasuState s) {
	return s.player == Player.Opponent && s.koma == Koma.Ou;
    }

    protected abstract Player getPlayerType();
}
