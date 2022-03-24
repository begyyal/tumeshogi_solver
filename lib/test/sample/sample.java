package sample;

import java.util.List;

import org.junit.jupiter.api.Test;

import begyyal.commons.object.collection.XGen;
import begyyal.shogi.TsSolver;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;
import begyyal.shogi.entity.TsKihuRecord;
import begyyal.shogi.entity.TsMasuState;
import begyyal.shogi.entity.TsMotigomaState;

public class sample {

    @Test
    public void hoge() {

	// 詰み手数(5手詰め)
	int numOfMoves = 5;

	// 盤面(空マス以外のマス情報の集合)
	var ban = XGen.<TsMasuState>newHashSet();
	ban.add(new TsMasuState(Player.Sente, TsKoma.Ryuu, 3, 3));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Gin, 2, 3));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Hu, 3, 2));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Ou, 1, 3));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Uma, 1, 5));

	// 持ち駒
	var motigoma = XGen.<TsMotigomaState>newHashSet();
	motigoma.add(new TsMotigomaState(Player.Sente, TsKoma.Keima, 1));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Hu, 17));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kyousya, 4));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Keima, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Gin, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kin, 4));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kaku, 1));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Hisya, 1));

	List<TsKihuRecord> kihu;
	try (var solver = new TsSolver()) {

	    // 計算実行
	    kihu = solver.calculate(numOfMoves, ban, motigoma);

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	    return;
	}

	// 結果は打ち順の棋譜のリスト(1手目~5手目)
	for (var kihuRecord : kihu) {
	    // kihuRecordの持つ情報は以下の通りです。

	    // public class TsKihuRecord {
	    //
	    // /** 先手/後手 */
	    // public final Player player;
	    // /** 移動元の筋(持駒からの場合は-1) */
	    // public final int fromSuzi;
	    // /** 移動元の段(持駒からの場合は-1) */
	    // public final int fromDan;
	    //
	    // /** 筋 */
	    // public final int suzi;
	    // /** 段 */
	    // public final int dan;
	    // /** 「同」フラグ */
	    // public final boolean dou;
	    // /** 駒種別(成駒含む/玉含まず/14種) */
	    // public final TsKoma koma;
	    // /** 相対位置(右/左) */
	    // public final KihuRel rel;
	    // /** 動作(上/寄/引/直) */
	    // public final KihuAct act;
	    // /** その他(成/不成/打) */
	    // public final KihuOpt opt;

	    // 例 -> ５２銀右上成
	    System.out.println(kihuRecord.toString());
	}
    }
}
