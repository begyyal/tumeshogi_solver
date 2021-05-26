package begyyal.shogi;

import begyyal.shogi.processor.BattleProcessor;

public class Entrypoint {

    public static void main(String args[]) {

	try {

	    if (args.length < 2)
		throw new IllegalArgumentException("Arguments lack.");

	    for (String str : BattleProcessor
		.of(args[0], args[1], args.length > 2 ? args[2] : null)
		.calculate())
		System.out.println(str);

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	}
    }
}
