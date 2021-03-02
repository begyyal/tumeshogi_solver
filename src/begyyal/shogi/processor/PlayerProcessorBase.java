package begyyal.shogi.processor;

import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.MasuState;

public abstract class PlayerProcessorBase {

    protected PlayerProcessorBase() {
    }

    protected MasuState occupy(MasuState from, MasuState to) {
	return from.player() == to.player() ? null 
		: new MasuState(
		    from.player(), 
		    from.koma(),
		    to.x(), 
		    to.y(), 
		    from.nariFlag() || to.y() <= 3,
		    to.rangedBy());
    }
    
    protected boolean canAdvanceTo(MasuState state) {
	return state != MasuState.Invalid
		&& (state.koma() == Koma.Empty || state.player() != getPlayerType());
    }
    
    protected abstract Player getPlayerType();
}
