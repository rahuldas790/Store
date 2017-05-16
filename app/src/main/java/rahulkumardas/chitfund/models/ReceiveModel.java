package rahulkumardas.chitfund.models;

import android.util.Log;

/**
 * Created by Rahul Kumar Das on 12-11-2016.
 */

public class ReceiveModel {

    public ReceiveModel(String id, String chitAmount, float paid, float gift, float pendingAmount, boolean fullSelected, boolean partiallySelected,
                        float monPaid, float monGift, float monRemain, String memberKey, String monthlyKey) {
        this.id = id;
        this.chitAmount = chitAmount;
        this.paid = paid;
        this.gift = gift;
        this.pendingAmount = pendingAmount;
        this.fullSelected = fullSelected;
        this.partiallySelected = partiallySelected;
        this.monGift = monGift;
        this.monPaid = monPaid;
        this.monRemain = monRemain;
        this.memberKey = memberKey;
        this.monthlyKey = monthlyKey;
        Log.i("Rahul", id);
        Log.i("Rahul", chitAmount);
        Log.i("Rahul", pendingAmount + "");
        Log.i("Rahul", fullSelected + "");
        Log.i("Rahul", partiallySelected + "");
        Log.i("Rahul", monPaid + "");
        Log.i("Rahul", monGift + "");
        Log.i("Rahul", monRemain + "");
    }

    public String chitAmount, id, memberKey, monthlyKey;
    public float pendingAmount, paid, gift, monPaid, monGift, monRemain, pay=0f, giftPay=0f;
    public boolean fullSelected, partiallySelected;
}
