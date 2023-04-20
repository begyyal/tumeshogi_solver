package it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import begyyal.commons.object.collection.XGen;
import begyyal.shogi.TsSolver;
import begyyal.shogi.def.common.Player;
import begyyal.shogi.def.common.TsKoma;
import begyyal.shogi.entity.TsKihuRecord;
import begyyal.shogi.entity.TsMasuState;
import begyyal.shogi.entity.TsMotigomaState;

public class te7 {

    @Test
    public void p1_issue251() {

	int numOfMoves = 7;

	var ban = XGen.<TsMasuState>newHashSet();
	ban.add(new TsMasuState(Player.Gote, TsKoma.Ou, 9, 4));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Kyousya, 9, 3));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Keima, 7, 3));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Kin, 8, 2));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Gin, 7, 4));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Gin, 8, 8));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Keima, 7, 5));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Gin, 9, 7));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Hisya, 8, 6));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Uma, 7, 7));

	var motigoma = XGen.<TsMotigomaState>newHashSet();
	motigoma.add(new TsMotigomaState(Player.Sente, TsKoma.Gin, 1));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Hu, 18));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kyousya, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Keima, 2));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kin, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kaku, 1));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Hisya, 1));

	List<TsKihuRecord> rec = null;
	try (var solver = new TsSolver()) {
	    rec = solver.calculate(numOfMoves, ban, motigoma);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Error occured.");
	}

	Objects.requireNonNull(rec);
	assertEquals(rec.size(), numOfMoves);

	assertEquals(rec.get(0).toString(), "９５銀");
	assertEquals(rec.get(1).toString(), "同玉");
	assertEquals(rec.get(2).toString(), "９６銀");
	assertEquals(rec.get(3).toString(), "９４玉");
	assertEquals(rec.get(4).toString(), "８３飛成");
	assertEquals(rec.get(5).toString(), "同銀");
	assertEquals(rec.get(6).toString(), "９５馬");
    }
}
