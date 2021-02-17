package begyyal.shogi;

import java.util.Arrays;

import begyyal.shogi.object.Ban;

public class Entrypoint {

    /**
     * 第1引数/持ち駒 -> x + 駒種別 + 枚数 ... + y + 駒種別 + 枚数
     * それ以降/盤面 -> 座標 + 自/相 + 駒種別 ( + 成りフラグ )
     * 
     * @param args 引数
     */
    public static void main(String args[]) {
	
	var ban = Ban.of(Arrays.copyOfRange(args, 1, args.length));

	// 【重要】
	// 持ち駒の考慮を入れる
	// 2歩を筆頭とした特殊ルール
	
	// *****************************
	
	// 1.
	// 自分の駒を探す
	// それぞれで可動域で動かす
	// 動かした先の可動域に王がいる場合、動かして相手に回す
	
	// 2.
	// 可動域に王がいるように持ち駒を打って相手に回す
	
	// *****************************
	
	// 1.
	// 王を動かして王手がかからないようにする
	
	// 2.
	// 駒を取って王手がかからないようにする

	// *****************************
	
	// パフォーマンスの話は後回し
    }
}
