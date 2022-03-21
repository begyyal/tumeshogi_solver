package begyyal.shogi;

import begyyal.shogi.log.TSLogger;
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

	TSLogger.print("processing time -> " + (System.currentTimeMillis() - start) + "msec");
    }

    private static MainSolver preProcess(String[] plainArgs) {
	var args = new ArgParser().exe(plainArgs);
	new ArgsValidator().validate(args);
	return new MainSolver(args);
    }
}
