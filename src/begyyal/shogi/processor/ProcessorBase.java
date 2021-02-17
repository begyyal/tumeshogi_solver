package begyyal.shogi.processor;

import java.util.function.Function;
import java.util.stream.IntStream;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.shogi.def.Koma;
import begyyal.shogi.def.Player;

public abstract class ProcessorBase {

    protected final SuperList<Koma> motigoma;
    
    protected ProcessorBase(Player player, String arg) {
	
	motigoma = SuperListGen.newi();
	int xIndex = arg.indexOf(Player.Self.id);
	int yIndex = arg.indexOf(Player.Opponent.id);
	
	Function<Integer,String> lowerGetter = idx -> arg.substring(1, idx);
	Function<Integer,String> higherGetter = idx -> arg.substring(idx + 1, arg.length());
	String argv = xIndex < yIndex 
		? (player == Player.Self ? lowerGetter.apply(xIndex) : higherGetter.apply(yIndex))  
		: (player == Player.Self ? higherGetter.apply(xIndex) : lowerGetter.apply(yIndex));
	
	for(int i = 0; i < argv.length() ; i += 2) {
	    var type = Koma.of(argv.substring(i, i+1));
	    IntStream.range(0, Integer.valueOf(argv.substring(i+1, i+2)))
	    	.forEach(idx -> motigoma.add(type)); 
	}
    }
    
}
