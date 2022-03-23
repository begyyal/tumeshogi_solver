package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import begyyal.commons.object.collection.XMap.XMapGen;
import begyyal.shogi.object.Args;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.object.Ban;

public class ArgsValidator {

    public ArgsValidator() {
    }

    public void validate(Args args) {

	if (args.numOfMoves % 2 != 1)
	    throw new IllegalArgumentException(
		"The argument of number of moves must be positive odd number.");

	validate(args.initBan);

	var ms = Arrays.stream(args.motigoma)
	    .flatMap(m -> IntStream.range(0, m.num).mapToObj(i -> m.koma.key));
	var tooMany = Stream.concat(args.initBan.matrixStream().map(s -> s.ss.koma.key), ms)
	    .filter(k -> !k.equals(Koma.Empty.key))
	    .collect(XMapGen.collect4count(k -> k))
	    .entrySet().stream()
	    .filter(e -> Koma.of(e.getKey(), false).numLimit < e.getValue())
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
		var state = ban.getState(x, y).ss;
		if (state.koma == Koma.Empty)
		    continue;
		existOu = existOu || state.koma == Koma.Ou && state.player == Player.Gote;
		if (state.koma == Koma.Hu
			&& (state.player == Player.Sente ? ++huCountX == 2 : ++huCountY == 2))
		    throw new IllegalArgumentException("It's 2hu.");
		if (!Ban.validateState(state.koma, x, y, state.player))
		    throw new IllegalArgumentException("There is invalid arrangement.");
	    }
	}

	if (!existOu)
	    throw new IllegalArgumentException("There must be the koma [Ou] in the ban.");
    }
}
