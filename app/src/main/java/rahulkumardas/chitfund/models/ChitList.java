package rahulkumardas.chitfund.models;

/**
 * Created by Rahul Kumar Das on 25-10-2016.
 */

public class ChitList {
    public ChitList(String id, boolean active, String amount, String date, String capacity, String available) {
        this.id = id;
        this.active = active;
        this.amount = amount;
        this.date = date;
        this.capacity = capacity;
        this.available = available;
    }

    public String id;
    public boolean active;
    public String amount;
    public String date;
    public String capacity;

    public String getAvailable() {
        return available;
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getCapacity() {
        return capacity;
    }

    public String available;

}
