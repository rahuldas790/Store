package rahulkumardas.chitfund.ui.gold;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class ApplyBidActivityGold extends AppCompatActivity {

    private String id;
    private DatabaseReference ref = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private final String GOLD = ChitFundApplication.GOLD;
    private List<String> names, allNames;
    private List<Float> monPending, pending;
    private String displayMonth, currentMonth, amount, capacity;
    private float defaultBidPayAmount;
    private TextView month;
    private AutoCompleteTextView name, bidAmount;
    private int displayMonthIndex = 1, currentMonthIndex = 0, membersIndex = 0;
    private ImageView home;
    private boolean found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_bid);

        home = (ImageView) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ApplyBidActivityGold.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        id = getIntent().getStringExtra("id");
        Log.i(ChitFundApplication.TAG, "Id is " + id);
        amount = getIntent().getStringExtra("amount");
        capacity = getIntent().getStringExtra("capacity");
        defaultBidPayAmount = getIntent().getFloatExtra("monthly", 0);
        ref = ref.child(USER + GOLD + "/Chits/" + id + "/");
        month = (TextView) findViewById(R.id.month);
        name = (AutoCompleteTextView) findViewById(R.id.name);
        bidAmount = (AutoCompleteTextView) findViewById(R.id.bidAmount);
        bidAmount.setVisibility(View.GONE);
        setAdapter();
    }

    private void setAdapter() {
        showProgress(this, "");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.i(ChitFundApplication.TAG, "Total Snapshot is " + dataSnapshot);
                    // this part is used to find the bid month from available months
                    // if last month is available then its the required bid month
                    // Otherwise checks for the months from start
                    long months = dataSnapshot.child("monthly").getChildrenCount();
                    if (dataSnapshot.child("monthly/" + months + "/default").getValue(Boolean.class)) {
                        displayMonth = dataSnapshot.child("monthly/" + months + "/name").getValue(String.class);
                        currentMonth = dataSnapshot.child("monthly/" + months + "/name").getValue(String.class);
                        displayMonthIndex = (int) months;
                        currentMonthIndex = (int) months;
                        found = true;
                    } else {
                        currentMonth = dataSnapshot.child("monthly/" + months + "/name").getValue(String.class);
                        currentMonthIndex = (int) months;
                        for (int i = 1; i <= months; i++) {
                            if (dataSnapshot.child("monthly/" + i + "/default").getValue(Boolean.class)) {
                                displayMonth = dataSnapshot.child("monthly/" + i + "/name").getValue(String.class);
                                displayMonthIndex = i;
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            AlertDialog.Builder ad = new AlertDialog.Builder(ApplyBidActivityGold.this);
                            ad.setTitle("Oops!");
                            ad.setMessage("No months available to apply bid");
                            ad.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            ad.setCancelable(false);
                            ad.show();
                        }
                    }
                    month.setText(displayMonth);


                    // this part is used to fetch the names of members
                    //one list is assigned for all the members allNames
                    //another list for the members who have not taken bid yet to be added to the autocomplete adapter
                    names = new ArrayList<String>();
                    allNames = new ArrayList<String>();
                    pending = new ArrayList<Float>();
                    monPending = new ArrayList<Float>();
                    long mems = dataSnapshot.child("members").getChildrenCount();
                    Log.i("Rahul", "No of members is " + mems);
                    for (int i = 0; i < mems; i++) {
                        String name = dataSnapshot.child("members/" + i + "/name").getValue(String.class);
                        Log.i("Rahul", "name is " + name);
                        if (dataSnapshot.child("members/" + i + "/bidMonth").getValue(String.class).equals("none")) {
                            names.add(name);
                            allNames.add(name);
                            pending.add(dataSnapshot.child("members/" + i + "/pending").getValue(Float.class));
                        } else {
                            allNames.add(name);
                        }
                    }
                    Log.i(ChitFundApplication.TAG, "Length is " + names.size() + " List is " + names);
                    name.setAdapter(new ArrayAdapter<String>(ApplyBidActivityGold.this, android.R.layout.simple_dropdown_item_1line, names));
                    dismissProgress();
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (databaseError != null) {
                    makeToast("Error Occured!", TastyToast.ERROR);
                    dismissProgress();
                    finish();
                }
            }
        });
    }

    public void applyBid(View view) {
        if (TextUtils.isEmpty(name.getText())) {
            makeToast("Empty Field(s)!", TastyToast.CONFUSING);
        } else {
            showProgress(this, "");
            membersIndex = allNames.indexOf(name.getText().toString());
            ref.child("members/" + membersIndex + "/bidMonth").setValue(displayMonth + "(" + displayMonthIndex + ")");
            ref.child("members/" + membersIndex + "/bidBy").setValue(currentMonth + "(" + currentMonthIndex + ")");

            ref.child("monthly/" + displayMonthIndex + "/default").setValue(false, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        finish();
                        makeToast("Transaction Success", TastyToast.SUCCESS);
                        dismissProgress();
                    } else {
                        makeToast("Transaction Failed", TastyToast.ERROR);
                        dismissProgress();
                    }
                }
            });

        }
    }
}
