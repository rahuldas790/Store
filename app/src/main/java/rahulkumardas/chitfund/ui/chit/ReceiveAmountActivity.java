package rahulkumardas.chitfund.ui.chit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.ReceiveModel;
import rahulkumardas.chitfund.adapter.ReceiveAmountAdapter;
import rahulkumardas.chitfund.ui.ChitFundApplication;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class ReceiveAmountActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = ChitFundApplication.TAG;
    private DatabaseReference ref = ChitFundApplication.reference;
    private DatabaseReference ref2 = ChitFundApplication.reference;
    private DatabaseReference ref3 = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private TextView totalRemaining, pay, remains;
    private Button calculate, receive;
    private static ListView list;
    private MaterialAutoCompleteTextView name;
    private List<String> names, visited; // visited list will keep record of the chitId+memberID already visited used when multiple instance of a membe in a chit
    private List<String> chits[];
    private static List<ReceiveModel> listAll;
    private int lastId = 0, position;// position is the id of the members which is selected from
    // autocomplete text view so that money details can be retrived
    // and update could be done
    private boolean foundChit = false;
    private float total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_amount);

        totalRemaining = (TextView) findViewById(R.id.totalPending);
        pay = (TextView) findViewById(R.id.totalAmount);
        remains = (TextView) findViewById(R.id.remains);
        calculate = (Button) findViewById(R.id.calculate);
        receive = (Button) findViewById(R.id.receive);
        list = (ListView) findViewById(R.id.recyclerView);
        name = (MaterialAutoCompleteTextView) findViewById(R.id.name);
        ref = ref.child(USER + "/Members/");
        ref2 = ref2.child(USER + "/Chits/");
        ref3 = ref3.child(USER);
        loadNames();

        name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Rahul", "position is "+position);
                getMoneyDetails();
            }
        });

        calculate.setOnClickListener(this);
        receive.setOnClickListener(this);
    }

    public static View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public static void addValue(int position, boolean full){
        View view = getViewByPosition(position, list);
        EditText pay = (EditText) view.findViewById(R.id.payAmount);
        EditText gift = (EditText) view.findViewById(R.id.payGift);
        LinearLayout payLayout = (LinearLayout)view.findViewById(R.id.payLayout);
        payLayout.setVisibility(View.VISIBLE);
        if(full) {
            pay.setText(listAll.get(position).pendingAmount + "");
            gift.setText(0 + "");
            ChitFundApplication.makeToast(listAll.get(position).pendingAmount+"", TastyToast.INFO);
        }else{
            pay.setText("0.0");
            gift.setText("0.0");
        }
    }

    private void getMoneyDetails() {
        position = names.indexOf(name.getText().toString());

        showProgress(this, "");
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float pending = 0f;
                Log.i("Rahul", "Main Snapshot is " + dataSnapshot);
                listAll = new ArrayList<ReceiveModel>(); // Initialize the list which contains all the data
                visited = new ArrayList<String>();// Initialize the visited list


                // loop through all the chitId's  the members is having i.e., applied for and has remaining amount;
                for (int i = 0; i < chits[position].size(); i++) {
                    String id = chits[position].get(i);
                    Log.i("Rahul", "Chits to search "+chits[position].size());

                    // iterate over all the chits
                    Iterator<DataSnapshot> chitIds = dataSnapshot.getChildren().iterator();
                    loop1:
                    while (chitIds.hasNext()) {

                        // match if the id of the chit and id of the chit of array ie equal
                        DataSnapshot snapshotChits = chitIds.next();
                        Log.i("Rahul", "Loop1 Snapshot is " + snapshotChits);
                        String chitId = snapshotChits.getKey();
                        if (chitId.equals(id)) {
                            foundChit = true;
                            String chitAmount = snapshotChits.child("/amount").getValue(String.class);
                            Iterator<DataSnapshot> members = snapshotChits.child("/members").getChildren().iterator();

                            // iterate over all the member of this chit and look for the member we have selected
                            loop2:
                            while (members.hasNext()) {
                                DataSnapshot memberList = members.next();
                                Log.i("Rahul", "Loop2 Snapshot is " + memberList);
                                String key = memberList.getKey();
                                String visit = chitId + key;

                                // check member name and if already visited
                                String name = memberList.child("name").getValue(String.class).trim();
                                if (name.equals(names.get(position)) && !visited.contains(visit)) {

                                    float remaining = memberList.child("pending").getValue(Float.class);
                                    pending += remaining;
                                    Log.i("Rahul", "Ramaining is " + remaining);

                                    // check if the member has already paid for this chit i.e, no pending
                                    if (remaining > 0) {
                                        float gift = memberList.child("gift").getValue(Float.class);
                                        float paid = memberList.child("paid").getValue(Float.class);

                                        Iterator<DataSnapshot> memberMonthlyList = memberList.child("monthly").getChildren().iterator();
                                        float monPaid = 0, monRemains = 0, monGift = 0;
                                        String memberMonthlyKey = "";
                                        DataSnapshot d = null;
                                        while (memberMonthlyList.hasNext()) {
                                            d = memberMonthlyList.next();
                                            Log.i("Rahul", " Snapshot is " + d);
                                            Log.i("Rahul ", "Snapshot is " + d);
                                            memberMonthlyKey = d.getKey();
                                        }
                                        Log.i("Rahul ", "Last Snapshot is " + memberMonthlyList);
                                        monPaid = d.child("paid").getValue(Float.class);
                                        monRemains = d.child("remain").getValue(Float.class);
                                        monGift = d.child("gift").getValue(Float.class);

                                        listAll.add(new ReceiveModel(chitId, chitAmount, paid, gift, remaining, false, false, monPaid, monGift, monRemains, key, memberMonthlyKey));
                                    }
                                    visited.add(visit);
                                    break loop2;
                                }

                            }

                            break loop1;
                        }

                    }
                    if(!foundChit){

                    }

                }
                ReceiveAmountAdapter adapter = new ReceiveAmountAdapter(listAll, ReceiveAmountActivity.this);
                list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                totalRemaining.setText(pending + "");
                dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadNames() {
        showProgress(this, "");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList<String>();

                long total = dataSnapshot.getChildrenCount();
                Iterator<DataSnapshot> memberList = dataSnapshot.getChildren().iterator();
                chits = new ArrayList[(int)total];
                int i=0;
                while(memberList.hasNext()) {
                    DataSnapshot member = memberList.next();
                    chits[i] = new ArrayList<String>();
                    String name = member.child("/name").getValue(String.class);
                    names.add(name.trim());
                    Log.i("Rahul", "Member name is "+name);

                    // this part iterate over all the chits the members has applied for and store the chit id into the array
                    Iterator<DataSnapshot> chitList = member.child("/chits").getChildren().iterator();
                    while (chitList.hasNext()) {
                        DataSnapshot chit = chitList.next();
                        chits[i].add(chit.getValue(String.class));
                    }
                    Log.i("Rahul", "Member "+i+" chit size "+chits[i].size());
                    i++;
                }

                name.setAdapter(new ArrayAdapter(ReceiveAmountActivity.this, android.R.layout.simple_dropdown_item_1line, names));
                dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calculate:
                total = 0f;
                try {
                    for (int i = 0; i < listAll.size(); i++) {

                        if (ReceiveAmountAdapter.payMoney[i] > 0 || ReceiveAmountAdapter.giftMoney[i] > 0) {
                            total += ReceiveAmountAdapter.payMoney[i];
                            total += ReceiveAmountAdapter.giftMoney[i];
                            lastId = i;
                        }
                    }
                    pay.setText(total + "");
                    remains.setText((Float.parseFloat(totalRemaining.getText().toString()) - total) + "");
                } catch (Exception e) {
                    Log.i("Rahul", "Error occurred by " + e.getMessage());
                }
                break;
            case R.id.receive:
                showProgress(ReceiveAmountActivity.this, "");
                for (int i = 0; i <= lastId; i++) {

                    float monPaid = listAll.get(i).monPaid;
                    float monGift = listAll.get(i).monGift;
                    float monRemain = listAll.get(i).monRemain;
                    float paid = listAll.get(i).paid;
                    float gift = listAll.get(i).gift;
                    float remain = listAll.get(i).pendingAmount;


                    if ((ReceiveAmountAdapter.giftMoney[i] > 0f || ReceiveAmountAdapter.payMoney[i] > 0f) && i != lastId) {
                        monPaid += ReceiveAmountAdapter.payMoney[i];
                        paid += ReceiveAmountAdapter.payMoney[i];
                        monGift += ReceiveAmountAdapter.giftMoney[i];
                        gift += ReceiveAmountAdapter.giftMoney[i];
                        remain = remain - (ReceiveAmountAdapter.payMoney[i] + ReceiveAmountAdapter.giftMoney[i]);
                        monRemain = monRemain - (ReceiveAmountAdapter.payMoney[i] + ReceiveAmountAdapter.giftMoney[i]);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/gift").setValue(gift);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/paid").setValue(paid);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/pending").setValue(remain);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/monthly/" + listAll.get(i).monthlyKey + "/paid").setValue(monPaid);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/monthly/" + listAll.get(i).monthlyKey + "/gift").setValue(monGift);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/monthly/" + listAll.get(i).monthlyKey + "/remain").setValue(monRemain);
                    } else if (i == lastId) {
                        monPaid += ReceiveAmountAdapter.payMoney[i];
                        paid += ReceiveAmountAdapter.payMoney[i];
                        monGift += ReceiveAmountAdapter.giftMoney[i];
                        gift += ReceiveAmountAdapter.giftMoney[i];
                        remain = remain - (ReceiveAmountAdapter.payMoney[i] + ReceiveAmountAdapter.giftMoney[i]);
                        monRemain = monRemain - (ReceiveAmountAdapter.payMoney[i] + ReceiveAmountAdapter.giftMoney[i]);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/gift").setValue(gift);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/paid").setValue(paid);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/pending").setValue(remain);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/monthly/" + listAll.get(i).monthlyKey + "/paid").setValue(monPaid);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/monthly/" + listAll.get(i).monthlyKey + "/gift").setValue(monGift);
                        ref2.child(listAll.get(i).id + "/members/" + listAll.get(i).memberKey + "/monthly/" + listAll.get(i).monthlyKey + "/remain").setValue(monRemain, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                dismissProgress();
                                makeToast("Value Received", TastyToast.SUCCESS);
                                finish();
                            }
                        });
                    }
                }

                String key = ref3.child("sheet").push().getKey();
                ref3.child("sheet/"+key+"/"+(new Date())).setValue(total);
                ref3.child("sheet/"+key+"/ref").setValue("chit");
        }
    }
}
