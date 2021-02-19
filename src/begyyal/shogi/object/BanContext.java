package begyyal.shogi.object;

import java.util.concurrent.atomic.AtomicInteger;

import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;

public class BanContext {

    private static final AtomicInteger idGen = new AtomicInteger();
    public final int id = idGen.getAndIncrement();
    private final SuperList<Ban> log; 
    
    private BanContext(String[] banStrs) {
	this.log = SuperListGen.of(Ban.of(banStrs));
    }

    private BanContext(SuperList<Ban> log) {
	this.log = log;
    }
    
    public Ban getLatestBan() {
	return this.log.getTip(); 
    }
    
    public void addLatestBan(Ban ban) {
	this.log.add(ban);
    }
    
    public BanContext copy() {
	return new BanContext(this.log);
    }
    
    public Ban[] getLog() {
	return this.log.toArray();
    }
    
    public static BanContext newi(String[] banStrs) {
	return new BanContext(banStrs);
    }
}
