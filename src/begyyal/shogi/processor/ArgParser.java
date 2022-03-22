package begyyal.shogi.processor;

import begyyal.commons.constant.Strs;
import begyyal.commons.object.XBool;
import begyyal.commons.object.collection.XGen;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.commons.util.function.XIntegers;
import begyyal.commons.util.function.XStrings;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.log.TSLogger;
import begyyal.shogi.object.Args;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.MasuState;
import begyyal.shogi.object.MotigomaState;

public class ArgParser {

    private final String motigomaArgRegex = "([xy]([a][1-9][1-8]?|[b-e][1-4]|[fg][12])+){1,2}";
    private final String banArgRegex = "([1-9][1-9][xy][a-dfg][z]?|[1-9][1-9][xy][eh])+";

    public ArgParser() {
    }

    public Args exe(String[] plainArgs) {

	var translate = XBool.newi();
	var argStrs = XListGen.of(plainArgs).stream()
	    .filter(a -> {
		boolean opt = false;
		if (opt = a.startsWith(Strs.hyphen))
		    for (int i = 1; i < a.length(); i++) {
			var c = a.charAt(i);
			if (c == 't')
			    translate.set(true);
			else if (c == 'd')
			    TSLogger.availability = true;
		    }
		return !opt;
	    }).toArray(String[]::new);

	if (argStrs.length < 2)
	    throw new IllegalArgumentException("Arguments lack.");

	var num = this.parseNumStr(argStrs[0]);
	var ban = this.parseBanStr(argStrs[1]);
	var motigoma = argStrs.length == 2
		? new MotigomaState[14]
		: this.parseMotigomaStr(argStrs[2]);

	return new Args(num, ban, motigoma, translate.get());
    }

    private int parseNumStr(String arg) {

	if (!XIntegers.checkIfParsable(arg))
	    throw new IllegalArgumentException(
		"The argument of number of moves must be number format.");

	int numOfMoves = Integer.parseInt(arg);
	if (numOfMoves % 2 != 1)
	    throw new IllegalArgumentException(
		"The argument of number of moves must be odd number.");

	return numOfMoves;
    }

    private Ban parseBanStr(String arg) {

	if (!arg.matches(banArgRegex))
	    throw new IllegalArgumentException("Ban argument format is invalid.");

	var matrix = new MasuState[81];

	String draft = arg;
	while (!draft.isBlank()) {

	    int skipIndex = XStrings.firstIndexOf(draft, "x", "y").v2;
	    var next = XStrings.firstIndexOf(draft.substring(skipIndex + 1), "x", "y");
	    int kiritori = next == null ? draft.length() : next.v2 + skipIndex - 1;
	    var masu = draft.substring(0, kiritori);
	    draft = kiritori == draft.length() ? Strs.empty : draft.substring(kiritori);

	    int x = 9 - Integer.valueOf(masu.substring(0, 1));
	    int y = 9 - Integer.valueOf(masu.substring(1, 2));
	    if (matrix[x * 9 + y] != null)
		throw new IllegalArgumentException(
		    "The masu states of [" + (9 - x) + Strs.hyphen + (9 - y) + "] are duplicated.");
	    matrix[x * 9 + y] = parseMasuStateStr(masu.substring(2), x, y);
	}

	var ban = new Ban(matrix);
	ban.setup();

	return ban;
    }

    private static MasuState parseMasuStateStr(String value, int x, int y) {

	var p = Player.of(value.substring(0, 1));
	if (p == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Player object.");

	var k = Koma.of(value.substring(1, 2));
	if (k == null)
	    throw new IllegalArgumentException("Can't parse [" + value + "] to Koma object.");

	boolean nari = value.length() > 2 && XStrings.equals(value.substring(2, 3), "z");

	return new MasuState(p, k, x, y, nari, XGen.newHashSet());
    }

    private MotigomaState[] parseMotigomaStr(String arg) {

	if (!arg.matches(motigomaArgRegex))
	    throw new IllegalArgumentException("Motigoma argument format is invalid.");

	var motigoma = new MotigomaState[14];
	int i = 0;
	for (var p : Player.values()) {
	    if (p == Player.None)
		continue;
	    for (var k : Koma.values()) {
		if (k == Koma.Ou || k == Koma.Empty)
		    continue;
		motigoma[i] = new MotigomaState(k, p, 0);
		i++;
	    }
	}

	int si = arg.indexOf(Player.Self.id), oi = arg.indexOf(Player.Opponent.id);
	int mi = si > oi ? si : oi;
	i = 1;
	Player p = Player.of(arg.substring(0, 1));

	while (i < arg.length()) {
	    String str = arg.substring(i, i + 1);
	    if (i == mi) {
		p = Player.of(str);
		i++;
		continue;
	    }
	    var koma = Koma.of(str);
	    var count = arg.substring(i + 1, i + 2);
	    if (i + 3 < arg.length()) { // 歩は保持数2桁があり得る
		var count2dig = arg.substring(i + 1, i + 3);
		if (XIntegers.checkIfParsable(count2dig) && ++i > 0)
		    count = count2dig;
	    }
	    motigoma[p.ordinal() * 7 + koma.ordinal()].num = Integer.parseInt(count);
	    i += 2;
	}

	return motigoma;
    }
}
