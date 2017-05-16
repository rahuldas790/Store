package rahulkumardas.chitfund.models;

/**
 * Created by Rahul Kumar Das on 22-01-2017.
 */

public class RentModel {
    public RentModel(String date, String tenant, String no) {
        this.date = date;
        this.tenant = tenant;
        this.no = no;
    }

    public String date, tenant, no;
}
