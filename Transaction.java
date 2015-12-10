import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zelengzhuang on 12/3/15.
 */
public class Transaction {
    public int beginTime;
    public ArrayList<Operation> myops;
    public HashSet<Integer> siteInvoved;
    public boolean isRO;

    public Transaction(int beginTime, boolean isRO) {
        this.isRO = isRO;
        this.beginTime = beginTime;
        this.myops = new ArrayList<>();
        this.siteInvoved = new HashSet<>();
    }
}
