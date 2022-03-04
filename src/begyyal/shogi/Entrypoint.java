package begyyal.shogi;

import begyyal.shogi.log.TSLogger;
import begyyal.shogi.object.Args;
import begyyal.shogi.processor.ArgParser;
import begyyal.shogi.processor.ArgsValidator;
import begyyal.shogi.processor.MainSolver;

public class Entrypoint {

    public static void main(String plainArgs[]) {

	long start = System.currentTimeMillis();

	try (var processor = preProcess(plainArgs)) {

	    for (String str : processor.calculate())
		System.out.println(str);

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	}

	TSLogger.print("processing time -> " + (System.currentTimeMillis() - start) / 1000 + "sec");
    }

    private static MainSolver preProcess(String[] plainArgs) {

	if (plainArgs.length < 2)
	    throw new IllegalArgumentException("Arguments lack.");

	var ap = new ArgParser();
	var motigomaStr = ap.parseTailArguments(plainArgs, 2);

	var numStr = plainArgs[0];
	var banStr = plainArgs[1];
	var motigomaPair = ap.parseMotigomaStr(motigomaStr);

	var args = new Args(
	    ap.parseNumStr(numStr),
	    ap.parseBanStr(banStr),
	    motigomaPair.v1,
	    motigomaPair.v2);
	
	new ArgsValidator().validate(args);

	return new MainSolver(args);
    }
}
