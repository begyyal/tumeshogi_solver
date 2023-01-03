package draft;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

	int numOfMoves = 7;

	var ban = XGen.<TsMasuState>newHashSet();
	ban.add(new TsMasuState(Player.Sente, TsKoma.Ryuu, 3, 1));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Gin, 2, 1));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Keima, 3, 5));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Kin, 3, 6));
	ban.add(new TsMasuState(Player.Sente, TsKoma.Hu, 2, 5));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Kyousya, 1, 1));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Ou, 1, 3));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Hisya, 4, 2));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Kaku, 4, 4));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Gin, 3, 3));
	ban.add(new TsMasuState(Player.Gote, TsKoma.Hu, 1, 4));
	
	var motigoma = XGen.<TsMotigomaState>newHashSet();
	motigoma.add(new TsMotigomaState(Player.Sente, TsKoma.Kaku, 1));
	motigoma.add(new TsMotigomaState(Player.Sente, TsKoma.Gin, 1));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Hu, 16));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kyousya, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Keima, 3));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Gin, 1));
	motigoma.add(new TsMotigomaState(Player.Gote, TsKoma.Kin, 3));

	List<TsKihuRecord> kihu;
	try (var solver = new TsSolver()) {
	    kihu = solver.calculate(numOfMoves, ban, motigoma);
	} catch (ExecutionException | InterruptedException | IOException e) {
	    System.out.println(e.getMessage());
	    for (var st : e.getStackTrace())
		System.out.println(st);
	    return;
	}
	
	for (var kihuRecord : kihu)
	    System.out.println(kihuRecord.toString());
    }
}
