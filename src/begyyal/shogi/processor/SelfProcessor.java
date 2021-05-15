package begyyal.shogi.processor;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.util.matrix.MatrixResolver;
import begyyal.commons.util.matrix.Vector;
import begyyal.commons.util.object.PairList;
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
	var moveSpread = ban
	    .search(s -> s.player == PlayerType)
	    .flatMap(s -> spreadMasuState(s, ban).map(dest -> Pair.of(s, dest)))
	    .collect(PairListGen.collect());
	var cs1 = moveSpread.stream()
	    .map(sp -> {
		var dest = sp.getRight();
		var newBan = ban.clone();
		var k = newBan.advance(sp.getLeft().x, sp.getLeft().y, dest.x, dest.y);
		var newDest = newBan.getState(dest.x, dest.y);
		return newBan.validateState(newDest) && newBan.isOuteBy(PlayerType, dest.x, dest.y)
			? context.branch(newBan, newDest, k, PlayerType, true)
			: null;
	    })
	    .filter(c -> c != null);

	// 開き王手
	var cs2 = getAkiOuteContextStream(ban, context, moveSpread);

	// 持ち駒配置による王手
	var cs3 = context.selfMotigoma
	    .stream()
	    .distinct()
	    .flatMap(k -> ban
		.search(s -> s.koma == Koma.Empty)
		.map(s -> Pair.of(k, s)))
	    .map(ks -> {
		var newBan = ban.clone();
		var s = newBan.deploy(ks.getLeft(), ks.getRight().x, ks.getRight().y, PlayerType);
		return s != null && newBan.isOuteBy(PlayerType, s.x, s.y)
			? context.branch(newBan, s, s.koma, PlayerType, false)
			: null;
	    })
	    .filter(c -> c != null);

	// 駒移動系は空き王手と他で重複し得るのでdistinctする
	return Stream.concat(Stream.concat(cs1, cs2).distinct(), cs3)
	    .toArray(BanContext[]::new);
    }

    private Stream<BanContext> getAkiOuteContextStream(
	Ban ban,
	BanContext context,
	PairList<MasuState, MasuState> moveSpread) {

	var candidates = ban.search(s -> s.player == PlayerType && MasuState.isLinearRange(s))
	    .toArray(MasuState[]::new);
	if (candidates.length == 0)
	    return Stream.empty();

	var opponentOu = ban
	    .search(s -> this.isOpponentOu(s))
	    .findFirst().get();

	return moveSpread.toMap()
	    .entrySet()
	    .stream()
	    .map(e -> Pair.of(e,
		Arrays.stream(candidates)
		    .map(c -> Pair.of(c, getAkiObstruction(ban, c).orElse(null)))
		    .filter(p -> p.getRight() != null && p.getRight().equals(e.getKey()))
		    .map(Pair::getLeft)
		    .findFirst()))
	    .filter(p -> p.getRight().isPresent())
	    .flatMap(t -> {
		var candidate = t.getRight().get();
		var decomposedOute = MatrixResolver.decompose(candidate.getVectorTo(opponentOu));
		return t.getLeft().getValue()
		    .stream()
		    .filter(s -> !ArrayUtils.contains(decomposedOute, candidate.getVectorTo(s)))
		    .map(s -> {
			var newBan = ban.clone();
			var from = t.getLeft().getKey();
			var k = newBan.advance(from.x, from.y, s.x, s.y);
			return newBan.validateState(s)
				? context.branch(newBan, s, k, PlayerType, true)
				: null;
		    })
		    .filter(c -> c != null);
	    });
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
	    if (result.player == Player.None)
		continue;
	    if (i == 0) {
		if (result.player == PlayerType) {
		    obstruction = result;
		    i++;
		} else
		    break;
	    } else if (i == 1)
		if (this.isOpponentOu(result)) {
		    return obstruction;
		} else
		    break;
	}

	return null;
    }

    @Override
    protected Player getPlayerType() {
	return PlayerType;
    }

    public static SelfProcessor newi() {
	return new SelfProcessor();
    }
}
