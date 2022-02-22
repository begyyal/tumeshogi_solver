package begyyal.shogi;

import begyyal.shogi.log.TSLogger;
import begyyal.shogi.object.Args;
import begyyal.shogi.processor.ArgParser;
import begyyal.shogi.processor.BattleProcessor;

public class Entrypoint {

    public static void main(String plainArgs[]) {

	long start = System.currentTimeMillis();
	try (var processor = BattleProcessor.newi()) {

	    if (plainArgs.length < 2)
		throw new IllegalArgumentException("Arguments lack.");

	    for (String str : processor.calculate(parseArgs(plainArgs)))
		System.out.println(str);

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	}
	TSLogger.print("processing time -> " + (System.currentTimeMillis() - start) / 1000 + "sec");
    }

    private static Args parseArgs(String[] plainArgs) {

	var ap = new ArgParser();
	var motigomaStr = ap.parseTailArguments(plainArgs, 2);

	var numStr = plainArgs[0];
	var banStr = plainArgs[1];
	var motigomaPair = ap.parseMotigomaStr(motigomaStr);

	return new Args(
	    ap.parseNumStr(numStr),
	    ap.parseBanStr(banStr),
	    motigomaPair.getLeft(),
	    motigomaPair.getRight());
    }
}
