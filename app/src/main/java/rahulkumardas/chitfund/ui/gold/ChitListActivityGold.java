package rahulkumardas.chitfund.ui.gold;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.ChitListAdapter;
import rahulkumardas.chitfund.models.ChitList;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class ChitListActivityGold extends AppCompatActivity implements AdapterView.OnItemClickListener {

    TextView month, noOfChit;
    ListView listView;
    DatabaseReference myRef = ChitFundApplication.reference;
    DatabaseReference myRef3 = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private final String GOLD = ChitFundApplication.GOLD;
    List<ChitList> list;
    private String monthNameFilter;
    private long count = 0;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chit_list);

        home = (ImageView) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChitListActivityGold.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        month = (TextView) findViewById(R.id.selected_month);
        noOfChit = (TextView) findViewById(R.id.no_of_chit);
        listView = (ListView) findViewById(R.id.recyclerView);
        month.setText(getIntent().getStringExtra("month"));
        monthNameFilter = getIntent().getStringExtra("month");
        myRef = myRef.child(USER + GOLD+"/Chits");
        myRef3 = myRef3.child(USER + "/Members");
        loadData();
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final int pos = position;
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ChitListActivityGold.this, listView);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.list_long_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("View")) {
                            String thisid = list.get(pos).id;
                            String amount = list.get(pos).amount;
                            String date = list.get(pos).date;
                            String available = list.get(pos).available;
                            String capacity = list.get(pos).capacity;
                            Intent i = new Intent(ChitListActivityGold.this, ChitDetailsActivityGold.class);
                            i.putExtra("id", thisid);
                            i.putExtra("amount", amount);
                            i.putExtra("date", date);
                            i.putExtra("capacity", capacity);
                            i.putExtra("available", available);
                            startActivity(i);
                        } else {
                            AlertDialog.Builder ad = new AlertDialog.Builder(ChitListActivityGold.this);
                            ad.setTitle("Warning!");
                            ad.setMessage("This will delete the chit from history and cannot be undone.\nAre you sure want to delete this chit?");
                            ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    myRef.child(list.get(pos).id).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            ChitFundApplication.makeToast("Chit deleted", TastyToast.SUCCESS);
                                        }
                                    });

                                }
                            });
                            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            ad.show();
                        }

                        return true;
                    }
                });

                popup.show(); //showing popup menu
                return false;
            }
        });
    }

    private void loadData() {

        showProgress(this, "Fetching data..");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list = new ArrayList<>();
                count = 0;
                try {
                    Iterator<DataSnapshot> listIterator = dataSnapshot.getChildren().iterator();
                    while (listIterator.hasNext()) {
                        DataSnapshot vals = listIterator.next();
                        String i = vals.getKey();
                        Log.i("Rahul", "Snapshot is " + vals);

                        if (vals.child("monthly/1/name").getValue(String.class).equals(monthNameFilter)) {
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
                                count++;
                                ChitList item = new ChitList(id, active, amount, date, capacity, avl + "");
                                list.add(item);
                            }
                        }
                    }
                    noOfChit.setText(count + "");
                    ChitListAdapter adapter = new ChitListAdapter(ChitListActivityGold.this, list);
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
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final int pos = position;
        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(ChitListActivityGold.this, listView);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.list_long_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().equals("View")) {
                    String thisid = list.get(position).id;
                    String amount = list.get(position).amount;
                    String date = list.get(position).date;
                    String available = list.get(position).available;
                    String capacity = list.get(position).capacity;
                    Intent i = new Intent(ChitListActivityGold.this, ChitDetailsActivityGold.class);
                    i.putExtra("id", thisid);
                    i.putExtra("amount", amount);
                    i.putExtra("date", date);
                    i.putExtra("capacity", capacity);
                    i.putExtra("available", available);
                    startActivity(i);
                } else {
                    AlertDialog.Builder ad = new AlertDialog.Builder(ChitListActivityGold.this);
                    ad.setTitle("Warning!");
                    ad.setMessage("This will delete the chit from history and cannot be undone.\nAre you sure want to delete this chit?");
                    ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            findAndDelete(list.get(position).id);
                            myRef.child(list.get(position).id).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    ChitFundApplication.makeToast("Chit deleted", TastyToast.SUCCESS);
                                }
                            });

                        }
                    });
                    ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }
                return true;
            }
        });

        popup.show(); //showing popup menu
    }

    private void findAndDelete(final String key){
        myRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> members = dataSnapshot.getChildren().iterator();
                String path  ="";
                while (members.hasNext()){
                    DataSnapshot member = members.next();

                    Iterator<DataSnapshot>  chits = member.child("golds").getChildren().iterator();
                    while (chits.hasNext()){
                        DataSnapshot chit = chits.next();

                        if(chit.getValue(String.class).equals(key)){
                            path = member.getKey()+"/golds/"+chit.getKey();
                            myRef3 = myRef3.child(path);
                            myRef3.removeValue();
                            myRef3 = ChitFundApplication.reference.child(USER + "/Members");
                        }


                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
