package rahulkumardas.chitfund.models;

/**
 * Created by Rahul Kumar Das on 02-03-2017.
 */

public class RentGroup {
    public RentGroup(String name, String date, int rent_count) {
        this.name = name;
        this.date = date;
        this.rent_count = rent_count;
    }

    public String name, date;
    public int rent_count;
}
