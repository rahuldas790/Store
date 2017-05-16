package rahulkumardas.chitfund.models;

/**
 * Created by Rahul Kumar Das on 28-11-2016.
 */

public class MemberDetails {

    public MemberDetails(String name, float pending, float paid, float gift) {
        this.name = name;
        this.pending = pending;
        this.paid = paid;
        this.gift = gift;
    }

    public String name;
    public float pending, paid, gift;

}
