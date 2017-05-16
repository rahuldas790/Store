package rahulkumardas.chitfund.ui.chit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
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

public class SearchMemberActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String TAG = ChitFundApplication.TAG;
    private String USER = ChitFundApplication.USER;
    private DatabaseReference ref = ChitFundApplication.reference;
    private DatabaseReference myRef = ChitFundApplication.reference;
    private AutoCompleteTextView searchETXT;
    private TextView name, chit;
    private EditText phone, address, email;
    private CardView cardView;
    private ListView listView;
    private List<String> names;
    private List<String> keys;
    private List<String> chitKeys;
    private List<ChitList> list;
    private ImageView edit;
    private boolean editEnter;
    private String key = "";
    private int background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_member);

        searchETXT = (AutoCompleteTextView) findViewById(R.id.search);
        listView = (ListView) findViewById(R.id.searchList);
        name = (TextView) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        phone.setKeyListener(null);
        email = (EditText) findViewById(R.id.email);
        email.setKeyListener(null);
        address = (EditText) findViewById(R.id.address);
        address.setKeyListener(null);
        cardView = (CardView) findViewById(R.id.cardView);
        chit = (TextView) findViewById(R.id.chits);
        cardView.setVisibility(View.GONE);
        chit.setVisibility(View.GONE);
        ref = ref.child(USER);
        myRef = myRef.child(USER + "/Chits");
        edit = (ImageView) findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editEnter) {
                    editEnter = false;
                    phone.setKeyListener(null);
                    phone.setBackgroundResource(android.R.color.transparent);
                    email.setKeyListener(null);
                    email.setBackgroundResource(android.R.color.transparent);
                    address.setKeyListener(null);
                    address.setBackgroundResource(android.R.color.transparent);
                    edit.setImageResource(R.mipmap.edit);
                    update();
                } else {
                    editEnter = true;
                    phone.setKeyListener(TextKeyListener.getInstance());
                    phone.setBackgroundResource(android.R.drawable.edit_text);
                    email.setKeyListener(TextKeyListener.getInstance());
                    email.setBackgroundResource(android.R.drawable.edit_text);
                    address.setKeyListener(TextKeyListener.getInstance());
                    address.setBackgroundResource(android.R.drawable.edit_text);
                    edit.setImageResource(R.drawable.ic_tick);
                }
            }
        });
        getNames();

        searchETXT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                key = keys.get(names.indexOf(searchETXT.getText().toString()));
                chit.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);
                searchETXT.setVisibility(View.GONE);
                getInfo(key);
            }
        });

        listView.setOnItemClickListener(this);
    }

    private void update() {
        ChitFundApplication.showProgress(this);
        ref.child("Members/" + key + "/phone").setValue(phone.getText().toString());
        ref.child("Members/" + key + "/email").setValue(email.getText().toString());
        ref.child("Members/" + key + "/address").setValue(address.getText().toString(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                dismissProgress();
                ChitFundApplication.makeToast("Success", TastyToast.SUCCESS);
            }
        });
    }

    private void getInfo(final String key) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> members = dataSnapshot.child("Members").getChildren().iterator();

                chitKeys = new ArrayList<String>();
                while (members.hasNext()) {
                    DataSnapshot member = members.next();
                    if (member.getKey().equalsIgnoreCase(key)) {
                        name.setText(member.child("name").getValue(String.class));
                        email.setText(member.child("email").getValue(String.class));
                        address.setText(member.child("address").getValue(String.class));
                        phone.setText(member.child("phone").getValue(String.class));

                        Iterator<DataSnapshot> chits = member.child("chits").getChildren().iterator();
                        while (chits.hasNext()) {
                            DataSnapshot chit = chits.next();
                            String id = chit.getValue(String.class);
                            chitKeys.add(id);
                        }
                        loadData();

                        break;
                    }
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
                try {
                    Iterator<DataSnapshot> members = dataSnapshot.child("Members").getChildren().iterator();
                    while (members.hasNext()) {
                        DataSnapshot member = members.next();
                        String name = member.child("name").getValue(String.class);
                        String phone = member.child("phone").getValue(String.class);
                        names.add(name + "  (" + phone + ")");
                        keys.add(member.getKey());
                    }
                    searchETXT.setAdapter(new ArrayAdapter<String>(SearchMemberActivity.this, android.R.layout.simple_dropdown_item_1line, names));
                    dismissProgress();
                } catch (Exception e) {

                }
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

                try {
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
                    ChitListAdapter adapter = new ChitListAdapter(SearchMemberActivity.this, list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    dismissProgress();
                } catch (Exception e) {
                    Log.i(ChitFundApplication.TAG, "Error is " + e.getMessage());
                }
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
        Intent i = new Intent(SearchMemberActivity.this, ChitDetailsActivity.class);
        i.putExtra("id", thisid);
        i.putExtra("amount", amount);
        i.putExtra("date", date);
        i.putExtra("capacity", capacity);
        i.putExtra("available", available);
        startActivity(i);
    }
}
