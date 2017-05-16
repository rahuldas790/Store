package rahulkumardas.chitfund.ui.chit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.AddMemberNameAdapter;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class AddMemberNameActivity extends AppCompatActivity implements View.OnClickListener {

    public static TextView countTv, submit;
    public static int count = 0, required;
    private String id, available, month;
    private ListView listView;
    public static boolean checked[];
    private ArrayList<String> names;
    private ArrayList<String> keys;
    private DatabaseReference ref1 = ChitFundApplication.reference;
    private DatabaseReference ref2 = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private long membreCount = 0, timeStamp;
    private float monthly;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        FloatingActionButton fam = (FloatingActionButton) findViewById(R.id.fam);
        fam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddMemberNameActivity.this, AddNewMember.class));
            }
        });

        home = (ImageView) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddMemberNameActivity.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        countTv = (TextView) findViewById(R.id.count);
        id = getIntent().getStringExtra("id");
        available = getIntent().getStringExtra("available");
        required = getIntent().getIntExtra("required", 0);
        monthly = getIntent().getFloatExtra("monthly", 0f);
        timeStamp = getIntent().getLongExtra("stamp", new Date().getTime());
        month = new SimpleDateFormat("MMMM").format(timeStamp);

        listView = (ListView) findViewById(R.id.recyclerView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (checked[position]) {
                    checked[position] = false;
                } else {
                    checked[position] = true;
                }
            }
        });
        submit = (TextView) findViewById(R.id.submit);
        submit.setOnClickListener(this);

        ref1 = ref1.child(USER + "/Members/");
        ref2 = ref2.child(USER + "/Chits/" + id + "/members/");
        loadData();
    }

    private void loadData() {
        showProgress(this, "");

        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList();
                keys = new ArrayList<String>();

                final long count = dataSnapshot.getChildrenCount();
                if (checked == null)
                    checked = new boolean[(int) count];

                Iterator<DataSnapshot> members = dataSnapshot.getChildren().iterator();
                while (members.hasNext()) {
                    DataSnapshot member = members.next();
                    names.add(member.child("name").getValue(String.class));
                    keys.add(member.getKey());
                }

                AddMemberNameAdapter adapter = new AddMemberNameAdapter(names, AddMemberNameActivity.this, checked);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getMonth(int n) {

        if (n == 2) {
            return "February";
        } else if (n == 3) {
            return "March";
        } else if (n == 4) {
            return "April";
        } else if (n == 5) {
            return "May";
        } else if (n == 6) {
            return "June";
        } else if (n == 7) {
            return "July";
        } else if (n == 8) {
            return "August";
        } else if (n == 9) {
            return "September";
        } else if (n == 10) {
            return "October";
        } else if (n == 11) {
            return "November";
        } else if (n == 12) {
            return "December";
        } else return "January";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                submit();
        }
    }

    private void submit() {

        count = 0;
        for (int i = 0; i < checked.length; i++) {
            if (checked[i])
                count++;
        }

        if (count == 0) {
            makeToast("Please select atleast one member", TastyToast.WARNING);
        } else if (count > required) {
            makeToast("Only " + required + " member can be added", TastyToast.WARNING);
        } else {

            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("Confirm!");
            ad.setMessage("Total member count = " + count + "\n\nAre you sure want to add these member to this chit?");
            ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AddNames().execute();
                    showProgress(AddMemberNameActivity.this, "");
                }
            });
            ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ad.show();

        }
    }


    public class AddNames extends AsyncTask{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ChitFundApplication.showProgress(AddMemberNameActivity.this);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            int last = 0;
            Map<String, Object> map, map2;

            // Adding the member names into the chit
            for (int i = 0, p = 0; i < checked.length; i++) {

                if (checked[i]) {
                    last = i;
                    long avail = Long.parseLong(available);
                    avail = avail + p;

                    map = new HashMap<>();
                    map2 = new HashMap<>();
                    map.put("name", names.get(i));
                    map.put("bidMonth", "none");
                    map.put("bidBy", "none");
                    map.put("gift", 0f);
                    map.put("pending", monthly);
                    map.put("paid", 0f);
                    ref2.child(avail+"").setValue(map);

                    map2.put("gift", 0f);
                    map2.put("paid", 0f);
                    map2.put("remain", monthly);

                    ref2.child(avail + "/monthly/" + month).setValue(map2);
                    p++;
                }

            }

            // Adding the chit index into the member chit list
            for (int i = 0; i < checked.length; i++) {
                if (checked[i] && last == i) {
                    String key = ref1.child(keys.get(i) + "/chits").push().getKey();
                    ref1.child(keys.get(i) + "/chits/" + key).setValue(id + "", new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            ChitFundApplication.dismissProgress();
                            MemberViewFragment.availableTxt.setText((Integer.parseInt(available) + count) + "");
                            MonthlyViewFragment.availableTxt.setText((Integer.parseInt(available) + count) + "");
                            ChitDetailsActivity.available = (Integer.parseInt(available) + count) + "";
                        }
                    });
                } else if (checked[i]) {
                    String key = ref1.child(keys.get(i) + "/chits").push().getKey();
                    ref1.child(keys.get(i) + "/chits/" + key).setValue(id + "");
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            finish();
            makeToast("Adding Success", TastyToast.SUCCESS);
        }
    }
    private void addNames() {
        ChitFundApplication.showProgress(this);
        int last = 0;
        Map<String, Object> map, map2;

        // Adding the member names into the chit
        for (int i = 0, p = 0; i < checked.length; i++) {

            if (checked[i]) {
                last = i;
                long avail = Long.parseLong(available);
                avail = avail + p;

                map = new HashMap<>();
                map2 = new HashMap<>();
                map.put("name", names.get(i));
                map.put("bidMonth", "none");
                map.put("bidBy", "none");
                map.put("gift", 0f);
                map.put("pending", monthly);
                map.put("paid", 0f);
                ref2.child(avail+"").setValue(map);

                map2.put("gift", 0f);
                map2.put("paid", 0f);
                map2.put("remain", monthly);

                ref2.child(avail + "/monthly/" + month).setValue(map2);
                p++;
            }

        }

        // Adding the chit index into the member chit list
        for (int i = 0; i < checked.length; i++) {
            if (checked[i] && last == i) {
                String key = ref1.child(keys.get(i) + "/chits").push().getKey();
                ref1.child(keys.get(i) + "/chits/" + key).setValue(id + "", new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        ChitFundApplication.dismissProgress();
                        MemberViewFragment.availableTxt.setText((Integer.parseInt(available) + count) + "");
                        MonthlyViewFragment.availableTxt.setText((Integer.parseInt(available) + count) + "");
                        ChitDetailsActivity.available = (Integer.parseInt(available) + count) + "";
                    }
                });
            } else if (checked[i]) {
                String key = ref1.child(keys.get(i) + "/chits").push().getKey();
                ref1.child(keys.get(i) + "/chits/" + key).setValue(id + "");
            }
        }
        finish();
        makeToast("Adding Success", TastyToast.SUCCESS);
    }
}
