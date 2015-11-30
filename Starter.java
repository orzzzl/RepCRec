import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by zelengzhuang on 11/30/15.
 */
public class Starter {

    private int ticks;
    private ArrayList<Operation> rec;

    public void UseFile(String filePath) {
        FileInputStream instream = null;
        try {
            instream = new FileInputStream(filePath);
            System.setIn(instream);
        } catch (Exception e) {}
    }

    public Operation ParseOperation(String inputRaw) {
        String input = inputRaw.toLowerCase();
        Operation ans;
        int N = input.length();
        int i = 0;
        String s1 = "", s2 = "";
        for (; i < N && input.charAt(i) != '('; i++) if (input.charAt(i) >= 'a' && input.charAt(i) <= 'z') s1 = s1 + input.charAt(i);
        if (s1.equals("dump")) {
            return new Operation("dump", "none", ticks);
        }
        i++;
        for (; i < N && input.charAt(i) != ')'; i++) if (input.charAt(i) != ' ' && input.charAt(i) != '\t') s2 = s2 + input.charAt(i);
        String[] tmp = s2.split(",");
        ans = new Operation(s1, tmp[0], ticks);
        if (s1.equals("r")) ans.SetTarget(new Variable(tmp[1]));
        if (s1.equals("w")) ans.SetTargetValue(new Variable(tmp[1]), Integer.parseInt(tmp[2]));
        return ans;
    }

    public void Report() {
        for (Operation i : rec) i.Report();
    }

    public void ProcessLine(String line) {
        if (line == null) return;
        for (String i : line.split(";")) rec.add(ParseOperation(i));
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            ProcessLine(in.nextLine());
            ticks++;
        }
    }
    public Starter() {
        ticks = 0;
        rec = new ArrayList<Operation>();
    }
}
