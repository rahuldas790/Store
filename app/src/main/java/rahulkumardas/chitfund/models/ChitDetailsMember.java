package rahulkumardas.chitfund.models;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Rahul Kumar Das on 27-10-2016.
 */

public class ChitDetailsMember {

    public ChitDetailsMember(String name, String gift, String paid, String pending, String bidMonth, String bidBy) {
        this.name = name;
        this.gift = gift;
        this.paid = paid;
        this.pending = pending;
        this.bidMonth = bidMonth;
        this.bidBy = bidBy;
    }

    public String name;
    public String gift;
    public String paid;
    public String pending;
    public String bidMonth;
    public String bidBy;
}
