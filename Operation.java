/**
 * Created by zelengzhuang on 11/30/15.
 */
public class Operation {
    public Variable target;
    public int value;
    public int timeStamp;
    public String type;
    public String name;

    public void SetTargetValue(Variable variable, int value) {
        this.target = variable;
        this.value = value;
    }

    public void SetTarget(Variable variable) {
        this.target = variable;
    }

    public Operation(String type, String transactionID, int timeStamp) {
        this.type = type;
        this.name = transactionID;
        this.timeStamp = timeStamp;
        this.target = new Variable();
    }

    public void Report() {
        System.out.printf("type: %s| timeStamp: %d| value: %d| targat: %d| transactionID: %s\n", type, timeStamp, value, target.index, name);
 //       System.out.printf("| timeStamp: %d| value: %d| \n", timeStamp, value);
    }


    public Operation() {
        this.type = "undefined";
        this.name = "undefined";
        this.target = new Variable();
        this.value = 0;
        this.timeStamp = 0;
    }
}
