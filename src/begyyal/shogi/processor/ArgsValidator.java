package begyyal.shogi.processor;

import java.util.stream.Stream;

import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.Ban;

public class ArgsValidator {

    public ArgsValidator() {
    }

    public void validate(Args args) {

	validate(args.initBan);

	var tooMany = Stream.concat(args.initBan.serializeMatrix().stream().map(s -> s.koma),
	    Stream.concat(args.selfMotigoma.stream(), args.opponentMotigoma.stream()))
	    .filter(k -> k != Koma.Empty)
	    .collect(SuperMapGen.collect(k -> k, k -> 1, (v1, v2) -> v1 + v2))
	    .entrySet()
	    .stream()
	    .filter(e -> e.getKey().numLimit < e.getValue())
	    .findFirst()
	    .orElse(null);
	if (tooMany != null)
	    throw new IllegalArgumentException("The koma [" + tooMany + "] exceeeds number limit.");
    }

    private void validate(Ban ban) {

	boolean existOu = false;
	for (int x = 0; x < 9; x++) {
	    int huCountX = 0, huCountY = 0;
	    for (int y = 0; y < 9; y++) {
		var state = ban.getState(x, y);
		if (state.koma == Koma.Empty)
		    continue;
		existOu = existOu || state.koma == Koma.Ou && state.player == Player.Opponent;
		if (state.koma == Koma.Hu && !state.nariFlag
			&& (state.player == Player.Self ? ++huCountX == 2 : ++huCountY == 2))
		    throw new IllegalArgumentException("It's 2hu.");
		if (!state.nariFlag && !Ban.validateState(state.koma, x, y, state.player))
		    throw new IllegalArgumentException("There is invalid arrangement.");
	    }
	}

	if (!existOu)
	    throw new IllegalArgumentException("There must be the koma [Ou] in y's arguments.");
    }
}
