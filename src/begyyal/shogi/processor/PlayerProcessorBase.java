package begyyal.shogi.processor;

import begyyal.shogi.object.MasuState;

public abstract class PlayerProcessorBase {

    protected PlayerProcessorBase() {
    }

    protected MasuState occupy(MasuState from, MasuState to) {
	return from.player() == to.player() ? null 
		: new MasuState(
		    from.player(), 
		    from.koma(),
		    to.suzi(), 
		    to.dan(), 
		    from.nariFlag() || to.dan() <= 3);
    }
}
