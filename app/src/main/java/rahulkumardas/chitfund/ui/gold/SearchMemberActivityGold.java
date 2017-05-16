package rahulkumardas.chitfund.ui.gold;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.ChitListAdapter;
import rahulkumardas.chitfund.models.ChitList;
import rahulkumardas.chitfund.ui.ChitFundApplication;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class SearchMemberActivityGold extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String TAG = ChitFundApplication.TAG;
    private String USER = ChitFundApplication.USER;
    private final String GOLD = ChitFundApplication.GOLD;
    private DatabaseReference ref = ChitFundApplication.reference;
    private DatabaseReference myRef = ChitFundApplication.reference;
    private AutoCompleteTextView searchETXT;
    private TextView name, phone, address, email, chit;
    private CardView cardView;
    private ListView listView;
    private List<String> names;
    private List<String> keys;
    private List<String> chitKeys;
    private List<ChitList> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_member);

        searchETXT = (AutoCompleteTextView) findViewById(R.id.search);
        listView = (ListView) findViewById(R.id.searchList);
        name = (TextView) findViewById(R.id.name);
        phone = (TextView) findViewById(R.id.phone);
        email = (TextView) findViewById(R.id.email);
        address = (TextView) findViewById(R.id.address);
        cardView = (CardView) findViewById(R.id.cardView);
        chit = (TextView) findViewById(R.id.chits);
        cardView.setVisibility(View.GONE);
        chit.setVisibility(View.GONE);
        ref = ref.child(USER+"/Members");
        myRef = myRef.child(USER + GOLD+"/Chits");
        getNames();

        searchETXT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = keys.get(names.indexOf(searchETXT.getText().toString()));
                chit.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);
                searchETXT.setVisibility(View.GONE);
                getInfo(key);
            }
        });

        listView.setOnItemClickListener(this);
    }

    private void getInfo(final String key) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Iterator<DataSnapshot> members = dataSnapshot.getChildren().iterator();

                    chitKeys = new ArrayList<String>();
                    while (members.hasNext()) {
                        DataSnapshot member = members.next();
                        if (member.getKey().equalsIgnoreCase(key)) {
                            name.setText(member.child("name").getValue(String.class));
                            phone.setText(member.child("email").getValue(String.class));
                            email.setText(member.child("address").getValue(String.class));
                            address.setText(member.child("phone").getValue(String.class));

                            Iterator<DataSnapshot> chits = member.child("golds").getChildren().iterator();
                            while (chits.hasNext()) {
                                DataSnapshot chit = chits.next();
                                String id = chit.getValue(String.class);
                                chitKeys.add(id);
                            }
                            loadData();

                            break;
                        }
                    }
                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getNames() {
        showProgress(this, "");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList<String>();
                keys = new ArrayList();

                Iterator<DataSnapshot> members = dataSnapshot.getChildren().iterator();
                while (members.hasNext()) {
                    DataSnapshot member = members.next();
                    String name = member.child("name").getValue(String.class);
                    String phone = member.child("phone").getValue(String.class);
                    names.add(name + "  (" + phone + ")");
                    keys.add(member.getKey());
                }
                searchETXT.setAdapter(new ArrayAdapter<String>(SearchMemberActivityGold.this, android.R.layout.simple_dropdown_item_1line, names));
                dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void back(View view) {
        finish();
    }

    private void loadData() {

        showProgress(this, "Fetching data..");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list = new ArrayList<>();

//                try {
                    for (int j = 0; j < chitKeys.size(); j++) {
                        String id1 = chitKeys.get(j);
                        Iterator<DataSnapshot> listIterator = dataSnapshot.getChildren().iterator();
                        while (listIterator.hasNext()) {
                            DataSnapshot vals = listIterator.next();
                            String i = vals.getKey();
                            if (i.equals(id1)) {
                                String id = i + "";
                                boolean active = vals.child("active").getValue(Boolean.class);
                                String amount = vals.child("amount").getValue(String.class);
                                String date = vals.child("date").getValue(String.class);
                                String capacity = vals.child("capacity").getValue(String.class);
                                long avl = 0;
                                if (vals.child("members").exists()) {
                                    avl = vals.child("members").getChildrenCount();
                                }
                                if (active) {
                                    ChitList item = new ChitList(id, active, amount, date, capacity, avl + "");
                                    list.add(item);
                                }

                                break;
                            }
                        }
                    }
                    ChitListAdapter adapter = new ChitListAdapter(SearchMemberActivityGold.this, list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    dismissProgress();
//                } catch (Exception e) {
//                    Log.i(ChitFundApplication.TAG, "Error is " + e.getMessage());
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ChitFundApplication.makeToast("Failed..", TastyToast.ERROR);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String thisid = list.get(position).id;
        String amount = list.get(position).amount;
        String date = list.get(position).date;
        String available = list.get(position).available;
        String capacity = list.get(position).capacity;
        Intent i = new Intent(SearchMemberActivityGold.this, ChitDetailsActivityGold.class);
        i.putExtra("id", thisid);
        i.putExtra("amount", amount);
        i.putExtra("date", date);
        i.putExtra("capacity", capacity);
        i.putExtra("available", available);
        startActivity(i);
    }
}
