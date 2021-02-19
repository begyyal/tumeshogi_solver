package begyyal.shogi;

import java.util.Arrays;

import begyyal.shogi.object.Ban;
import begyyal.shogi.processor.BattleProcessor;

public class Entrypoint {

    /**
     * 第1引数/持ち駒 -> x + 駒種別 + 枚数 ... + y + 駒種別 + 枚数
     * それ以降/盤面 -> 座標 + 自/相 + 駒種別 ( + 成りフラグ )
     * 
     * @param args 引数
     */
    public static void main(String args[]) {
	
	try {
	    var battle = BattleProcessor.of(args[0], Arrays.copyOfRange(args, 1, args.length));
	}catch(Exception e) {
	    System.out.println(e.getMessage());
	    System.out.println(e.getStackTrace());
	}
    }
}
