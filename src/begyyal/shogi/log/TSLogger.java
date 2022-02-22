package begyyal.shogi.log;

public class TSLogger {
    public static boolean availability = false;
    private static final String const1 = "[DEBUG][FROM] ";
    private static final String const2 = "[DEBUG][PRINT] ";

    public static void print(Object target) {
	if (!availability)
	    return;
	System.out.println(const1 + Thread.currentThread().getStackTrace()[2]);
	System.out.println(const2 + target);
    }

    public static boolean isDebug() {
	return availability;
    }
}
