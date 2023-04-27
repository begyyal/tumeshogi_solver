package begyyal.shogi.object.cache;

import java.util.Arrays;
import java.util.Objects;

public class ContextCacheKey {
    public final Object[] key;
    public final int hash;

    public ContextCacheKey(Object[] key) {
	this.key = key;
	this.hash = Objects.hash(key);
    }

    @Override
    public boolean equals(Object o) {
	// 用途が限定的なので負荷抑止を優先
	return Arrays.equals(this.key, ((ContextCacheKey) o).key);
    }

    @Override
    public int hashCode() {
	return hash;
    }
}
