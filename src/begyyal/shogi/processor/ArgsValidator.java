package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import begyyal.commons.object.collection.XMap.XMapGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.Ban;

public class ArgsValidator {

    public ArgsValidator() {
    }

    public void validate(Args args) {

	validate(args.initBan);

	var ms = Arrays.stream(args.motigoma)
	    .flatMap(m -> IntStream.range(0, m.num).mapToObj(i -> m.koma));
	var tooMany = Stream.concat(args.initBan.matrixStream().map(s -> s.koma), ms)
	    .filter(k -> k != Koma.Empty)
	    .collect(XMapGen.collect4count(k -> k))
	    .entrySet().stream()
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
