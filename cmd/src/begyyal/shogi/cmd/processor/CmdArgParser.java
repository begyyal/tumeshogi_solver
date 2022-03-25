package begyyal.shogi.cmd.processor;

import java.util.Set;

import begyyal.commons.constant.Strs;
import begyyal.commons.object.XBool;
import begyyal.commons.object.collection.XGen;
import begyyal.commons.object.collection.XList.XListGen;
import begyyal.commons.util.function.XIntegers;
import begyyal.commons.util.function.XStrings;
import begyyal.shogi.cmd.object.CmdArgs;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;
import begyyal.shogi.entity.TsMasuState;
import begyyal.shogi.entity.TsMotigomaState;

public class CmdArgParser {

    private final String motigomaArgRegex = "([xy]([a][1-9][1-8]?|[b-e][1-4]|[fg][12])+){1,2}";
    private final String banArgRegex = "([1-9][1-9][xy][a-dfg][z]?|[1-9][1-9][xy][eh])+";

    public CmdArgParser() {
    }

    public CmdArgs exe(String[] plainArgs) {

	var translate = XBool.newi();
	var dispTime = XBool.newi();
	var argStrs = XListGen.of(plainArgs).stream()
	    .filter(a -> {
		boolean opt = false;
		if (opt = a.startsWith(Strs.hyphen))
		    for (int i = 1; i < a.length(); i++) {
			var c = a.charAt(i);
			if (c == 't')
			    translate.set(true);
			else if (c == 'd')
			    dispTime.set(true);
		    }
		return !opt;
	    }).toArray(String[]::new);

	if (argStrs.length < 2)
	    throw new IllegalArgumentException("Arguments lack.");

	var num = this.parseNumStr(argStrs[0]);
	var ban = this.parseBanStr(argStrs[1]);
	var motigoma = argStrs.length == 2
		? XGen.<TsMotigomaState>newHashSet()
		: this.parseMotigomaStr(argStrs[2]);

	return new CmdArgs(num, ban, motigoma, translate.get(), dispTime.get());
    }

    private int parseNumStr(String arg) {
	if (!XIntegers.checkIfParsable(arg))
	    throw new IllegalArgumentException(
		"The argument of number of moves must be number format.");
	return Integer.parseInt(arg);
    }

    private Set<TsMasuState> parseBanStr(String arg) {

	if (!arg.matches(banArgRegex))
	    throw new IllegalArgumentException("Ban argument format is invalid.");

	var ban = XGen.<TsMasuState>newHashSet();
	String draft = arg;
	while (!draft.isBlank()) {
	    int skipIndex = XStrings.firstIndexOf(draft, "x", "y").v2;
	    var next = XStrings.firstIndexOf(draft.substring(skipIndex + 1), "x", "y");
	    int kiritori = next == null ? draft.length() : next.v2 + skipIndex - 1;
	    var masu = draft.substring(0, kiritori);
	    draft = kiritori == draft.length() ? Strs.empty : draft.substring(kiritori);
	    ban.add(parseMasuStateStr(masu));
	}

	return ban;
    }

    private static TsMasuState parseMasuStateStr(String value) {
	int suzi = Integer.valueOf(value.substring(0, 1));
	int dan = Integer.valueOf(value.substring(1, 2));
	var p = Player.of(value.substring(2, 3));
	boolean nari = value.length() > 4 && XStrings.equals(value.substring(4, 5), "z");
	var k = TsKoma.of(value.substring(3, 4), nari);
	return new TsMasuState(p, k, suzi, dan);
    }

    private Set<TsMotigomaState> parseMotigomaStr(String arg) {

	var motigoma = XGen.<TsMotigomaState>newHashSet();
	if (!arg.matches(motigomaArgRegex))
	    throw new IllegalArgumentException("Motigoma argument format is invalid.");

	int si = arg.indexOf(Player.Sente.key), gi = arg.indexOf(Player.Gote.key);
	int mi = si > gi ? si : gi, i = 1;
	Player p = Player.of(arg.substring(0, 1));

	while (i < arg.length()) {
	    String str = arg.substring(i, i + 1);
	    if (i == mi) {
		p = Player.of(str);
		i++;
		continue;
	    }
	    var koma = TsKoma.of(str, false);
	    var count = arg.substring(i + 1, i + 2);
	    if (i + 3 < arg.length()) { // 歩は保持数2桁があり得る
		var count2dig = arg.substring(i + 1, i + 3);
		if (XIntegers.checkIfParsable(count2dig) && ++i > 0)
		    count = count2dig;
	    }
	    motigoma.add(new TsMotigomaState(p, koma, Integer.parseInt(count)));
	    i += 2;
	}

	return motigoma;
    }
}
