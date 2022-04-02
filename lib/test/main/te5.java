package main;

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

public class te5 {

    @Test
    public void p6_issue161() {

	long start = System.currentTimeMillis();
	int numOfMoves = 5;

	var ban = XGen.<TsMasuState>newHashSet();
	ban.add(new TsMasuState(Player.Gote, TsKoma.Gin, 1, 1));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Hu, 1, 2));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Ou, 2, 1));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Hu, 3, 1));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Kin, 3, 2));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Kaku, 5, 4));

	var motigoma = XGen.<TsMotigomaState>newHashSet();
	motigoma.add(new TsMotigomaState(Player.Sente, TsKoma.Hisya, 1));
	motigoma.add(new TsMotigomaState(Player.Sente, TsKoma.Kin, 1));
	motigoma.add(new TsMotigomaState(Player.Sente, TsKoma.Keima, 1));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Hu, 16));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kyousya, 4));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Keima, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Gin, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kin, 1));
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

	assertEquals(rec.get(0).toString(), "２３飛");
	assertEquals(rec.get(1).toString(), "２２銀");
	assertEquals(rec.get(2).toString(), "３３桂");
	assertEquals(rec.get(3).toString(), "１１玉");
	assertEquals(rec.get(4).toString(), "２１金");

	System.out.println("te5_6 - " + (System.currentTimeMillis() - start) + "msec");
    }

}
