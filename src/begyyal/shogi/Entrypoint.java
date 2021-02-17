package begyyal.shogi;

import begyyal.shogi.object.Ban;

public class Entrypoint {

    public static void main(String args[]) {
	
	var ban = Ban.of(args);

	// 自分の駒を探す
	// 1. それぞれで可動域で動かす
	// 動かした先の可動域に王がいるか？
	// いる場合、1で動かして相手に回す
	// 王を動かして王手がかからないようにする (????)
	
	// !!!!!! 王手の逆引きが必要 !!!!!!!!!
	// 並列必須
	
    }
}
