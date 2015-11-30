/**
 * Created by zelengzhuang on 11/30/15.
 */
public class Variable {
    public String name;
    public int index;
    public int copy;
    public int value;

    private void ParseName() {
        String indexPart = "";
        int i = 0;
        for (; i < name.length(); i++) {
            if (name.charAt(i) >= '0' && name.charAt(i) <= '9') indexPart = indexPart + name.charAt(i);
            if (name.charAt(i) == '.') break;
        }
        index = Integer.parseInt(indexPart);
        value = 10 * index;
        String copyPart = "";
        for (; i < name.length(); i++) {
            if (name.charAt(i) >= '0' && name.charAt(i) <= '9') copyPart = copyPart + name.charAt(i);
        }
        if (copyPart.length() > 0) {
            copy = Integer.parseInt(copyPart);
        } else {
            copy = 0;
        }
    }

    public Variable(String name) {
        this.name = name;
        ParseName();
    }

    public Variable() {
        name = "undefined";
        index = 0;
        copy = 0;
        value = 0;
    }

    public int GetSite() {
        if (index % 2 == 0) return -1;
        else return (1 + index % 10);
    }
}
