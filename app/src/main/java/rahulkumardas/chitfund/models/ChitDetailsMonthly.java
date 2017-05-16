package rahulkumardas.chitfund.models;

import java.util.ArrayList;

/**
 * Created by Rahul Kumar Das on 25-10-2016.
 */

public class ChitDetailsMonthly {

    public String name;
    public String amount;
    public String bid;
    public String type;
    public String by;
    public String takenOn;

    public ChitDetailsMonthly(String name, String amount, String bid, String type, String by, String takenOn) {
        this.name = name;
        this.amount = amount;
        this.bid = bid;
        this.type = type;
        this.by = by;
        this.takenOn = takenOn;
    }



}
