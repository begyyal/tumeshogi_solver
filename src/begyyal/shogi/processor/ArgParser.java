package begyyal.shogi.processor;

import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.function.SuperStrings;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.log.TSLogger;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.MasuState;

public class ArgParser {

    private final String motigomaArgRegex = "([xy]([a][1-9][1-8]?|[b-e][1-4]|[fg][12])+){1,2}";
    private final String banArgRegex = "([1-9][1-9][xy][a-dfg][z]?|[1-9][1-9][xy][eh])+";
    
    public ArgParser() {
    }

    public int parseNumStr(String arg) {

	if (!NumberUtils.isParsable(arg))
	    throw new IllegalArgumentException(
		"The argument of number of moves must be number format.");

	int numOfMoves = Integer.parseInt(arg);
	if (numOfMoves % 2 != 1)
	    throw new IllegalArgumentException(
		"The argument of number of moves must be odd number.");

	return numOfMoves;
    }

    public Ban parseBanStr(String arg) {

	if (!arg.matches(banArgRegex))
	    throw new IllegalArgumentException("Ban argument format is invalid.");

	var matrix = new MasuState[9][9];

	String draft = arg;
	while (!draft.isBlank()) {

	    int skipIndex = SuperStrings.firstIndexOf(draft, "x", "y").getRight();
	    var next = SuperStrings.firstIndexOf(draft.substring(skipIndex + 1), "x", "y");
	    int kiritori = next == null ? draft.length() : next.getRight() + skipIndex - 1;
	    var masu = draft.substring(0, kiritori);
	    draft = kiritori == draft.length() ? Strs.empty : draft.substring(kiritori);

	    int x = Integer.valueOf(masu.substring(0, 1));
	    int y = Integer.valueOf(masu.substring(1, 2));
	    if (matrix[9 - x][9 - y] != null)
		throw new IllegalArgumentException(
		    "The masu states of [" + x + "-" + y + "] are duplicated.");
	    matrix[9 - x][9 - y] = MasuState.of(masu.substring(2), x, y);
	}


	var ban = new Ban(matrix);
	ban.setup();
	
	return ban;
    }

    public Pair<SuperList<Koma>, SuperList<Koma>> parseMotigomaStr(String arg) {

	if (!arg.matches(motigomaArgRegex))
	    throw new IllegalArgumentException("Motigoma argument format is invalid.");

	var selfMotigoma = SuperListGen.<Koma>newi();
	var opponentMotigoma = SuperListGen.<Koma>newi();
	var motigomaPair = Pair.of(selfMotigoma, opponentMotigoma);

	int xIndex = arg.indexOf(Player.Self.id);
	int yIndex = arg.indexOf(Player.Opponent.id);
	var single = xIndex < 0 ? opponentMotigoma : yIndex < 0 ? selfMotigoma : null;
	if (single != null) {
	    getMotigomaParser(single).accept(arg.substring(1));
	    return motigomaPair;
	}

	boolean xy = xIndex < yIndex;
	var low = xy ? selfMotigoma : opponentMotigoma;
	var high = xy ? opponentMotigoma : selfMotigoma;
	getMotigomaParser(low).accept(arg.substring(1, xy ? yIndex : xIndex));
	getMotigomaParser(high).accept(arg.substring((xy ? yIndex : xIndex) + 1, arg.length()));

	return motigomaPair;
    }

    private Consumer<String> getMotigomaParser(SuperList<Koma> motigoma) {
	return arg -> {
	    int i = 0;
	    while (i < arg.length()) {
		var type = Koma.of(arg.substring(i, i + 1));
		var count = arg.substring(i + 1, i + 2);
		if (i + 3 < arg.length()) { // 歩は保持数2桁があり得る
		    var count2dig = arg.substring(i + 1, i + 3);
		    if (StringUtils.isNumeric(count2dig) && ++i > 0)
			count = count2dig;
		}
		IntStream.range(0, Integer.valueOf(count)).forEach(idx -> motigoma.add(type));
		i += 2;
	    }
	};
    }

    public String parseTailArguments(String[] args, int offset) {
	if (args.length == offset)
	    return null;
	int idx = -1;
	for (int count = offset; count < args.length; count++)
	    if ("debug".equals(args[count]))
		idx = count;
	if (idx < 0)
	    return args[offset];
	TSLogger.availability = true;
	return idx == offset ? null : args[offset];
    }
}
