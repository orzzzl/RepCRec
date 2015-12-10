/**
 * Created by zelengzhuang on 12/3/15.
 */
public class ValueUnit {
    public int commitTime, lockTime, value;
    //saves commit time, and time that lock was acquired
    public ValueUnit(int ct, int lt, int v) {
        this.commitTime = ct;
        this.lockTime = lt;
        this.value = v;
    }
    
    public boolean isCommitEarlier(int t) {
        return this.commitTime < t;
    }

    public boolean isLockEarlier(int t) {
        return this.lockTime < t;
    }

    public int getValue() {
        return this.value;
    }
}
