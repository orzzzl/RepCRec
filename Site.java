import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Created by zelengzhuang on 11/30/15.
 */
public class Site {

    public boolean isGood;
    public int siteNo;

    public HashMap<Integer, ArrayList<ValueUnit> > values;
    public HashMap<Integer, String> writeLocks;
    public HashMap<Integer, ArrayList<String> > readLocks;


    public Site(int siteNo) {
        this.siteNo = siteNo;
        values = new HashMap<>();
        readLocks = new HashMap<>();
        writeLocks = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            if (i % 2 == 0 || (i % 10 + 1) == siteNo) {
                values.put(i, new ArrayList<>());
                values.get(i).add(new ValueUnit(-1, -1, i * 10));
            }
        }
        isGood = true;
    }

    public boolean canRead(String transactionId, int variable) {
        if (isGood == false) return false;
        if (writeLocks.containsKey(variable) && writeLocks.get(variable).compareTo(transactionId) != 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canWrite(String transactionId, int variable) {
        if (isGood == false) return false;
        if (readLocks.containsKey(variable) && readLocks.get(variable) != null &&
                (readLocks.get(variable).size() > 1) ||
                (readLocks.size() == 1 && !readLocks.get(variable).get(0).equals(transactionId))) {
            return false;
        }
        if (writeLocks.containsKey(variable) && writeLocks.get(variable).compareTo(transactionId) != 0) {
            return false;
        }
        return true;
    }

    public boolean putReadLock(String transactionId, int variable) {
        if (isGood == false) return false;
        if (canRead(transactionId, variable) == false) return false;
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(transactionId);
        if (this.readLocks.containsKey(variable) == false) this.readLocks.put(variable, tmp);
        else this.readLocks.get(variable).add(transactionId);
        return true;
    }

    public boolean putWriteLock(String transactionId, int variable) {
        if (isGood == false) return false;
        if (canWrite(transactionId, variable) == false) return false;
        this.writeLocks.put(variable, transactionId);
        return true;
    }

    public void freeLocks(String transactionId) {
        if (isGood == false) return;
        for (int i = 1; i <= 20; i++) {
            if (readLocks.containsKey(i) && readLocks.get(i).contains(transactionId)) {
                readLocks.get(i).remove(transactionId);
            }
            if (writeLocks.containsKey(i) && writeLocks.get(i).compareTo(transactionId) == 0) {
                writeLocks.remove(i);
            }
        }
    }

    public boolean isVariableHere(int variable) {
        return values.containsKey(variable);
    }

    public boolean writeDatabase(int variable, int ct, int lt, int v) {
        if (isGood == false) return false;
        if (isVariableHere(variable) == false) return false;
        values.get(variable).add(new ValueUnit(ct, lt, v));
        return true;
    }

    public int readBeforeCommitTimeT(int variable, int t) {
        if (isGood == false) return Integer.MIN_VALUE;
        if (isVariableHere(variable) == false) return Integer.MIN_VALUE;
        ArrayList<ValueUnit> tmp = values.get(variable);
        int ans = Integer.MIN_VALUE, tmax = Integer.MIN_VALUE;
        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i).isCommitEarlier(t) && tmp.get(i).commitTime >= tmax) {
                tmax = tmp.get(i).commitTime;
                ans = tmp.get(i).getValue();
            }
        }
        return ans;
    }

    public int readBeforeLockTimeT(int variable, int t) {
        if (isGood == false) return Integer.MIN_VALUE;
        if (isVariableHere(variable) == false) return Integer.MIN_VALUE;
        ArrayList<ValueUnit> tmp = values.get(variable);
        int ans = Integer.MIN_VALUE, tmax = Integer.MIN_VALUE;
        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i).isLockEarlier(t) && tmp.get(i).lockTime >= tmax) {
                tmax = tmp.get(i).lockTime;
                ans = tmp.get(i).getValue();
            }
        }
        return ans;
    }

    public void fail() {
        this.isGood = false;
    }

    public void recover() {
        this.isGood = true;
    }
}
