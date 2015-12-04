import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zelengzhuang on 12/3/15.
 */
public class TransactionManager {
    public Site[] sites;
    public HashMap<String, Transaction> transactions;
    public boolean verbose;

    public TransactionManager() {
        verbose = true;
        this.transactions = new HashMap<>();
        this.sites = new Site[11];
        for (int i = 1; i <= 10; i++) {
            this.sites[i] = new Site(i);
        }
    }

    public void removeTransaction(String transactionID, Boolean isAbort) {
        if (transactions.containsKey(transactionID)) {
            Transaction tmp = transactions.get(transactionID);
            Iterator it = tmp.siteInvoved.iterator();
            while (it.hasNext()) {
                int siteVal = (int)it.next();
                if (siteVal == -1) {
                    for (int i = 1; i <= 10; i++) {
                        if (isAbort)
                            System.out.println(transactionID + " abort...  freeing all locks of " + transactionID + " on site " + i);
                        sites[i].freeLocks(transactionID);
                    }
                } else {
                    if (isAbort)
                        System.out.println(transactionID + " abort...  freeing all locks of " + transactionID + " on site " + siteVal);
                    sites[siteVal].freeLocks(transactionID);
                }
            }
            transactions.remove(transactionID);
        }
    }

    public void processOperation(Operation op) {
        if (op.type.equals("dump")) {
            dump(op);
            return;
        }
        if (op.type.equals("fail")) {
            fail(op);
        } else if (op.type.equals("recover")) {
            recover(op);
        } else if (op.type.equals("begin")) {
            transanctionBorn(op, false);
        } else if (op.type.equals("beginro")) {
            transanctionBorn(op, true);
        } else {
            if (transactions.containsKey(op.name) == false) return;
            if (op.type.equals("end")) {
                commit(op);
            } else if (op.type.equals("r")) {
                readHandler(op);
            } else if (op.type.equals("w")) {
                writeHandler(op);
            }
        }
    }

    private void transanctionBorn(Operation op, boolean isRO) {
        if (verbose) {
            System.out.println("time: " + op.timeStamp + " transaction " + op.name + " is created..." + " Is it RO? " + isRO);
        }
        this.transactions.put(op.name, new Transaction(op.timeStamp, isRO));
    }

    private void commit(Operation op) {
        if (verbose) {
            System.out.println("time: " + op.timeStamp + " transaction " + op.name + " is commited...");
        }
        Transaction cur = transactions.get(op.name);
        for (int i = 0; i < cur.myops.size(); i++) {
            Operation now = cur.myops.get(i);
            int siteNo = now.target.GetSite();
            if (siteNo == -1) {
               siteNo = getFirstGoodSite();
            }
            if (now.type.equals("r")) {
                System.out.printf("//////////////////  ");
                int val = -1;
                if (cur.isRO) {
                    val = sites[siteNo].readBeforeCommitTimeT(now.target.index, cur.beginTime);
                } else {
                    val = sites[siteNo].readBeforeLockTimeT(now.target.index, now.timeStamp);
                }
                System.out.println(now.name + " read from variable " + now.target.name + ", value is " + val);
            } else {
                sites[siteNo].writeDatabase(now.target.index, op.timeStamp, now.timeStamp, now.value);
            }
        }
        removeTransaction(op.name, false);
    }

    private void readHandler(Operation op) {
        if (verbose) {
            System.out.println("time: " + op.timeStamp + " transaction " + op.name + " read " + op.target.name);
        }
        transactions.get(op.name).siteInvoved.add(op.target.GetSite());
        transactions.get(op.name).myops.add(op);
        int siteNo = op.target.GetSite();
        if (transactions.get(op.name).isRO) {
            if (siteNo == -1) {
                boolean flag = false;
                for (int i = 1; i <= 10; i++) flag = (flag || sites[i].isGood);
                if (flag == false) removeTransaction(op.name, true);
            } else if (sites[op.target.GetSite()].isGood == false) removeTransaction(op.name, true);
            return;
        }
        boolean needAbort = true;
        String selected = op.name;
        if (siteNo == -1) {
            for (int i = 1; i <= 10; i++) {
                if (sites[i].canRead(op.name, op.target.index)) {
                    sites[i].putReadLock(op.name, op.target.index);
                    needAbort = false;
                } else {
                    selected = decideWhoAbort(selected, sites[i].writeLocks.get(op.target.index));
                }
            }
            if (needAbort && selected.equals(op.name)) removeTransaction(selected, true);
        } else {
            if (sites[siteNo].canRead(op.name, op.target.index)) {
                sites[siteNo].putReadLock(op.name, op.target.index);
            } else {
                selected = decideWhoAbort(selected, sites[siteNo].writeLocks.get(op.target.index));
                if (selected.equals("ERROR")) {
                    System.err.println("error in select a transaction to abbort");
                    return;
                }
                if (selected.equals(op.name)) removeTransaction(selected, true);
            }
        }
    }

    private void writeHandler(Operation op) {
        if (verbose) {
            System.out.println("time: " + op.timeStamp + " transaction " + op.name + " write " + op.target.name + " with value " + op.value);
        }
        transactions.get(op.name).siteInvoved.add(op.target.GetSite());
        transactions.get(op.name).myops.add(op);
        int siteNo = op.target.GetSite();
        boolean needAbort = true;
        String selected = op.name;
        if (siteNo == -1) {
            for (int i = 1; i <= 10; i++) {
                if (sites[i].canWrite(op.name, op.target.index)) {
                    sites[i].putWriteLock(op.name, op.target.index);
                    needAbort = false;
                } else {
                    if (sites[i].readLocks.containsKey(op.target.index)) {
                        for (String ele : sites[i].readLocks.get(op.target.index)) {
                            selected = decideWhoAbort(selected, ele);
                        }
                    }
                    if (sites[i].writeLocks.containsKey(op.target.index))
                        selected = decideWhoAbort(selected, sites[i].writeLocks.get(op.target.index));
                }
            }
            if (needAbort && selected.equals(op.name)) removeTransaction(selected, true);
        } else {
            if (sites[siteNo].canWrite(op.name, op.target.index)) {
                sites[siteNo].putWriteLock(op.name, op.target.index);
            } else {
                if (sites[siteNo].readLocks.containsKey(op.target.index)) {
                    for (String ele : sites[siteNo].readLocks.get(op.target.index)) {
                        selected = decideWhoAbort(selected, ele);
                    }
                }
                if (sites[siteNo].writeLocks.containsKey(op.target.index))
                    selected = decideWhoAbort(selected, sites[siteNo].writeLocks.get(op.target.index));
                if (selected.equals(op.name)) removeTransaction(selected, true);
            }
        }
    }

    private String decideWhoAbort(String t1, String t2) {
        if (transactions.containsKey(t1) && transactions.containsKey(t2)) {
            if (transactions.get(t1).beginTime > transactions.get(t2).beginTime) {
                return t1;
            } else {
                return t2;
            }
        } else {
            return "ERROR";
        }
    }

    private void fail(Operation op) {
        if (verbose) {
            System.out.println("time: " + op.timeStamp + " site " + op.name + " failed!!");
        }
        sites[Integer.parseInt(op.name)].fail();
        Iterator it = transactions.entrySet().iterator();
        ArrayList<String> abortTransactions = new ArrayList<>();
        for (Map.Entry<String, Transaction> entry : transactions.entrySet()) {
            String key = entry.getKey();
            Transaction value = entry.getValue();
            if (value.siteInvoved.contains(Integer.parseInt(op.name)) || value.siteInvoved.contains(-1)) {
                abortTransactions.add(key);
            }
        }
        for (String ele : abortTransactions) removeTransaction(ele, true);
    }

    private void dump(Operation op) {
        System.out.println("=== output of dump");
        int cnt = 0;
        for (int i = 1; i <= 10; i++) if (sites[i].isGood) cnt++;
        for (int i = 1; i <= 20; i++) {
            if (i % 2 == 0) {
                int k = getFirstGoodSite();
                int val = sites[k].readBeforeCommitTimeT(i, op.timeStamp);
                System.out.println("x" + i + ": " + val + " || at " + cnt + " good sites, read from the first good site: " + k);
            } else {
                int val = sites[1 + i % 10].readBeforeCommitTimeT(i, op.timeStamp);
                System.out.println("x" + i + ": " + val + " || read from site: " + (1 + i % 10));
            }
        }
    }

    private int getFirstGoodSite() {
        int siteNo = -1;
        for (int k = 1; k <= 10; k++) {
            if (sites[k].isGood) {
                siteNo = k;
                break;
            }
        }
        return siteNo;
    }
    private  void recover(Operation op) {
        if (verbose) {
            System.out.println("time: " + op.timeStamp + " site " + op.name + " recovered!!");
        }
        sites[Integer.parseInt(op.name)].recover();
    }
}
