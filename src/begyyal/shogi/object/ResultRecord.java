package begyyal.shogi.object;

public class ResultRecord {
    public final int id;
    public final MasuState state;

    public ResultRecord(
	int id,
	MasuState state) {
	this.id = id;
	this.state = state;
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof ResultRecord))
	    return false;
	var casted = (ResultRecord) o;
	return casted.id == id;
    }

    @Override
    public int hashCode() {
	return id;
    }
}
