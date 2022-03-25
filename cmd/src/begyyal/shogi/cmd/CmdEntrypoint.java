package begyyal.shogi.cmd;

import begyyal.shogi.TsSolver;
import begyyal.shogi.cmd.processor.CmdArgParser;
import begyyal.shogi.cmd.processor.Summarizer;

public class CmdEntrypoint {

    public static void main(String plainArgs[]) {

	long start = System.currentTimeMillis();
	var args = new CmdArgParser().exe(plainArgs);

	try (var solver = new TsSolver()) {
	    var kihu = solver.calculate(args.numOfMoves, args.ban, args.motigoma);
	    for (String str : new Summarizer(args.translate).exe(kihu))
		System.out.println(str);

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	}

	if (args.dispTime)
	    System.out.println(
		"processing time -> " + (System.currentTimeMillis() - start) + "msec");
    }
}
