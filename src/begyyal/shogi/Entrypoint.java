package begyyal.shogi;

import begyyal.shogi.processor.BattleProcessor;

public class Entrypoint {

    public static void main(String args[]) {

	long start = System.currentTimeMillis();
	try (var processor = BattleProcessor.newi()) {

	    if (args.length < 2)
		throw new IllegalArgumentException("Arguments lack.");

	    for (String str : processor.calculate(args[0], args[1], parseTailArguments(args, 2)))
		System.out.println(str);

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	}
	TRLogger.print("processing time -> " + (System.currentTimeMillis() - start) / 1000 + "sec");
    }

    private static String parseTailArguments(String[] args, int offset) {
	if (args.length == offset)
	    return null;
	int idx = -1;
	for (int count = offset; count < args.length; count++)
	    if ("debug".equals(args[count]))
		idx = count;
	if (idx < 0)
	    return args[offset];
	TRLogger.isAvailable = true;
	return idx == offset ? null : args[offset];
    }

    public static class TRLogger {

	private static boolean isAvailable = false;
	private static final String const1 = "[DEBUG][FROM]";
	private static final String const2 = "[DEBUG][PRINT]";

	public static void print(Object target) {
	    if (!isAvailable)
		return;
	    System.out.println(const1 + Thread.currentThread().getStackTrace()[2]);
	    System.out.println(const2 + target);
	}

	public static boolean isDebug() {
	    return isAvailable;
	}
    }
}
