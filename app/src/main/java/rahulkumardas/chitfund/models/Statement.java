package rahulkumardas.chitfund.models;

/**
 * Created by Rahul Kumar Das on 22-01-2017.
 */

public class Statement {

    public Statement(String date, String description, String type, String mode, float amount) {
        this.date = date;
        this.description = description;
        this.type = type;
        this.mode = mode;
        this.amount = amount;
    }

    public Statement(String date, String description, String type, float amount) {
        this.date = date;
        this.description = description;
        this.type = type;
        this.amount = amount;
    }

    public String date, description, type, mode="";
    public float amount;
}
