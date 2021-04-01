package begyyal.shogi;

import begyyal.shogi.processor.BattleProcessor;

public class Entrypoint {

    /**
     * 第1引数/持ち駒 -> x + 駒種別 + 枚数 ... + y + 駒種別 + 枚数 <br>
     * 第2引数/盤面 -> 座標 + 自/相 + 駒種別 ( +成りフラグ )
     * 
     * @param args 引数
     */
    public static void main(String args[]) {

	try {
	    for (String str : BattleProcessor
		.of(args[0], args[1])
		.calculate())
		System.out.println(str);

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	}
    }
}
