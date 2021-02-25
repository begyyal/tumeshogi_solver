package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.PairList.PairListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;
import begyyal.shogi.object.Ban;
import begyyal.shogi.object.BanContext;
import begyyal.shogi.object.MasuState;

public class SelfProcessor extends PlayerProcessorBase {

    public static final Player PlayerType = Player.Self;

    private SelfProcessor() {
	super();
    }

    public BanContext[] spread(BanContext context) {

	var ban = context.getLatestBan();

	// 駒の移動による王手(開き王手は除く)
	var moveSpread = ban.search(s -> s.player() == PlayerType)
	    .flatMap(s -> spreadMasuState(s, ban).map(dest -> Pair.of(s, dest)));
	var contextStream1 = moveSpread
	    .filter(sp -> getOuteFilter(ban).test(sp.getLeft()))
	    .map(sp -> {
		var newBan = ban.clone();
		var k = newBan.advance(sp.getLeft(), sp.getRight(), PlayerType);
		return context.branch(newBan, sp.getRight(), k, PlayerType, true);
	    });

	// 持ち駒配置による王手
	var contextStream2 = context.selfMotigoma
	    .stream()
	    .flatMap(k -> ban
		.search(s -> s.koma() == Koma.Empty)
		.map(s -> new MasuState(PlayerType, k, s.suzi(), s.dan(), false))
		.filter(getOuteFilter(ban)))
	    .map(s -> {
		var newBan = ban.clone();
		newBan.advance(s);
		return context.branch(newBan, s, s.koma(), PlayerType, false);
	    });

	// 開き王手
	var contextStream3 = getAkiOuteContextStream(ban, moveSpread);

	return Stream.concat(
	    contextStream1,
	    contextStream2).toArray(BanContext[]::new);
    }

    private Stream<BanContext> getAkiOuteContextStream(
	Ban ban,
	Stream<Pair<MasuState, MasuState>> moveSpread) {

	var candidates = ban.search(s -> s.player() == PlayerType &&
		(s.koma() == Koma.Kyousha && s.nariFlag() == false ||
			s.koma() == Koma.Hisha ||
			s.koma() == Koma.Kaku));
	if (candidates.count() == 0)
	    return Stream.empty();

	var moveSpreadMap = moveSpread.collect(PairListGen.collect()).toMap();
	var ineffectiveKeys = moveSpreadMap
	    .keySet()
	    .stream()
	    .filter(k -> !candidates.map(c -> getAkiObstruction(ban, c).orElse(null))
		.filter(s -> s != null && s.equals(k))
		.findFirst()
		.isPresent())
	    .toArray();
	for(var k : ineffectiveKeys)
    	    moveSpreadMap.remove(k);
	
	// 軌道上の移動は除外しないとだめ
//	return moveSpreadMap.entrySet()
//		.stream()
//		.map(e -> null)
	
	return null;
    }

    private Predicate<MasuState> getOuteFilter(Ban ban) {
	return s -> ban.validateState(s) && spreadMasuState(s, ban)
	    .anyMatch(s2 -> s2.koma() == Koma.Ou && s2.player() != PlayerType);
    }

    // 引数のステートから移動可能なステート全てを移動後のステートにしてStreamで返却
    private Stream<MasuState> spreadMasuState(MasuState state, Ban ban) {
	return state.getTerritory()
	    .stream()
	    .flatMap(v -> spreadVector(ban, v, state));
    }

    private Stream<MasuState> spreadVector(
	Ban ban,
	Vector v,
	MasuState s) {

	if (Math.abs(v.x()) == 1 || Math.abs(v.y()) == 1) {
	    var result = ban.exploration(s, v);
	    return canAdvanceTo(result) ? Stream.of(occupy(s, result)) : Stream.empty();
	}

	var stateBucket = new MasuState[8];
	int i = 0;
	for (var miniV : MatrixResolver.decompose(v)) {
	    var result = ban.exploration(s, miniV);
	    if (canAdvanceTo(result)) {
		stateBucket[i] = occupy(s, result);
		i++;
	    }
	    if (result == MasuState.Invalid || result.koma() != Koma.Empty)
		break;
	}

	return i == 0 ? Stream.empty() : Arrays.stream(stateBucket, 0, i);
    }

    // 1ステートからの空き王手は複数にならない
    private Optional<MasuState> getAkiObstruction(
	Ban ban,
	MasuState state) {
	return state.getTerritory()
	    .stream()
	    .map(v -> getAkiObstructionOnVector(ban, v, state))
	    .filter(s -> s != null)
	    .findFirst();
    }

    private MasuState getAkiObstructionOnVector(
	Ban ban,
	Vector v,
	MasuState state) {

	var decomposed = MatrixResolver.decompose(v);
	if (decomposed.length == 1)
	    return null;

	int i = 0;
	MasuState obstruction = null;
	for (var miniV : MatrixResolver.decompose(v)) {

	    var result = ban.exploration(state, miniV);

	    if (result == MasuState.Invalid)
		break;
	    if (result.player() == Player.None)
		continue;
	    if (i == 0) {
		if (result.player() == PlayerType) {
		    obstruction = result;
		    i++;
		} else
		    break;
	    } else if (i == 1)
		if (result.player() != PlayerType && result.koma() == Koma.Ou) {
		    return obstruction;
		} else
		    break;
	}

	return null;
    }

    private boolean canAdvanceTo(MasuState state) {
	return state != MasuState.Invalid
		&& (state.koma() == Koma.Empty || state.player() != PlayerType);
    }

    public static SelfProcessor newi() {
	return new SelfProcessor();
    }
}
