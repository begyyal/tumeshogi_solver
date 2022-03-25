package begyyal.shogi.processor;

import java.util.Objects;
import java.util.Set;

import begyyal.commons.constant.Strs;
import begyyal.commons.object.collection.XGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.entity.TsMasuState;
import begyyal.shogi.entity.TsMotigomaState;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.MasuState;
import begyyal.shogi.object.MotigomaState;

public class ArgConverter {

    public ArgConverter() {
    }

    public Args exe(
	int numOfMoves,
	Set<TsMasuState> masuStateSet,
	Set<TsMotigomaState> motigomaStateSet) {
	Objects.requireNonNull(masuStateSet);
	Objects.requireNonNull(motigomaStateSet);
	return new Args(numOfMoves, createBan(masuStateSet), convertMotigoma(motigomaStateSet));
    }

    private Ban createBan(Set<TsMasuState> stateSet) {

	var matrix = new MasuState[81];
	for (int i = 0; i < 81; i++) {
	    int x = i / 9, y = i % 9;
	    var sArray = stateSet.stream()
		.filter(s -> s != null && s.suzi == 9 - x && s.dan == 9 - y)
		.toArray(TsMasuState[]::new);
	    if (sArray.length == 1) {
		var state = sArray[0];
		matrix[i] = state == null
			? MasuState.emptyOf(x, y, XGen.newHashSet())
			: new MasuState(state.player, Koma.of(state.koma), x, y, XGen.newHashSet());
	    } else if (sArray.length > 1)
		throw new IllegalArgumentException(
		    "The masu states of [" + (9 - x) + Strs.hyphen + (9 - y) + "] are duplicated.");
	}

	var ban = new Ban(matrix);
	for (int i = 0; i < 81; i++)
	    ban.markRangeBy(i);

	return ban;
    }

    private MotigomaState[] convertMotigoma(Set<TsMotigomaState> stateSet) {

	var motigoma = new MotigomaState[14];
	int i = 0;
	for (var p : Player.values()) {
	    for (var k : Koma.values()) {
		if (k.nari || k == Koma.Ou || k == Koma.Empty)
		    continue;
		var state = stateSet.stream()
		    .filter(s -> s != null && Koma.of(s.koma) == k && s.player == p)
		    .findFirst().orElse(null);
		motigoma[i] = new MotigomaState(k, p, state == null ? 0 : state.num);
		i++;
	    }
	}

	return motigoma;
    }
}
