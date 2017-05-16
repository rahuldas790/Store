package rahulkumardas.chitfund.models;

/**
 * Created by Rahul Kumar Das on 28-12-2016.
 */

public class ReceiveModelRent {
    public ReceiveModelRent(String date, float paid, float remains) {
        this.date = date;
        this.paid = paid;
        this.remains = remains;
    }

    public String date;
    public float paid, remains;
}
