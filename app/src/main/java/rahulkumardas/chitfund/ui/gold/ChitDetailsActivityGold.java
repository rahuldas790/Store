package rahulkumardas.chitfund.ui.gold;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.ChitDetailsMember;
import rahulkumardas.chitfund.models.ChitDetailsMonthly;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;

import static rahulkumardas.chitfund.ui.ChitFundApplication.TAG;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;

public class ChitDetailsActivityGold extends AppCompatActivity implements View.OnClickListener {


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String id, amount, date, capacity;
    public static String available;
    private DatabaseReference ref = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private final String GOLD = ChitFundApplication.GOLD;
    private long months = 0;
    private String lastMonth, newMonthName;
    private float lastAmount;
    private long members = 0, timeStamp;
    private ArrayList<Float> pendings;
    private ImageButton add;
    private boolean updateDone = false;
    private Thread thread;
    private ImageView home, jumpTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chit_details);

        add = (ImageButton) findViewById(R.id.add);
        add.setOnClickListener(this);
        jumpTo = (ImageButton) findViewById(R.id.jump_to_date);
        jumpTo.setOnClickListener(this);
        id = getIntent().getStringExtra("id");
        amount = getIntent().getStringExtra("amount");
        date = getIntent().getStringExtra("date");
        capacity = getIntent().getStringExtra("capacity");
        available = getIntent().getStringExtra("available");
        home = (ImageView) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChitDetailsActivityGold.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        ref = ref.child(USER + GOLD + "/Chits/" + id + "/");
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);

        ChitFundApplication.showProgress(this);
        setStamp();

    }

    private void setStamp() {

        ref.child("currentStamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError == null) {
                    checkUpdate();
                } else {
                    makeToast("Connection Failed", TastyToast.ERROR);
                    finish();
                }
            }
        });

    }

    private void checkUpdate() {
        Log.i(TAG, "Inside checkUpdate() method");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.i(TAG, "Full Snapshot " + dataSnapshot);
                    long lastStamp = dataSnapshot.child("lastStamp").getValue(Long.class);
                    long currentStamp = dataSnapshot.child("currentStamp").getValue(Long.class);
                    timeStamp = currentStamp;
                    ChitFundApplication.dismissProgress();
                    long diff = currentStamp - lastStamp;
                    final long ONE_MONTH_DUR = 2629746000L;
                    final long TEN_MIN_DUR = 600000L;
                    Log.i(TAG, "Monthly difference is " + diff);
                    if (diff >= TEN_MIN_DUR && !updateDone) {

                        months = dataSnapshot.child("monthly").getChildrenCount();
                        members = dataSnapshot.child("members").getChildrenCount();
                        lastMonth = dataSnapshot.child("monthly/" + months + "/name").getValue(String.class);
                        lastAmount = Float.parseFloat(dataSnapshot.child("monthly/" + months + "/amount").getValue(String.class));
                        Iterator<DataSnapshot> members = dataSnapshot.child("members").getChildren().iterator();
                        pendings = new ArrayList<Float>();
                        DataSnapshot member = null;
                        while (members.hasNext()) {
                            member = members.next();
                            pendings.add(member.child("pending").getValue(Float.class));
                        }

                        new AlertDialog.Builder(ChitDetailsActivityGold.this)
                                .setTitle("Update!")
                                .setMessage("Hi,\nIts one month past the last update. Do you want to update now?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        updateDone = true;
                                        AlertDialog.Builder ad = new AlertDialog.Builder(ChitDetailsActivityGold.this);
                                        LayoutInflater inflater = ChitDetailsActivityGold.this.getLayoutInflater();
                                        View dialogView = inflater.inflate(R.layout.gold_rate_monthly_popup, null);
                                        final AlertDialog alert = ad.setView(dialogView).create();
                                        final MaterialEditText rate = (MaterialEditText) dialogView.findViewById(R.id.goldRate);
                                        Button done = (Button) dialogView.findViewById(R.id.done);
                                        done.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (TextUtils.isEmpty(rate.getText())) {
                                                    rate.setError("Field is empty!");
                                                } else {
                                                    updateData(rate.getText().toString());
                                                    alert.dismiss();
                                                }
                                            }
                                        });
                                        alert.show();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        months = dataSnapshot.child("monthly").getChildrenCount();
                        lastAmount = Float.parseFloat(dataSnapshot.child("monthly/" + months + "/amount").getValue(String.class));
                        lastMonth = dataSnapshot.child("monthly/" + months + "/name").getValue(String.class);
                    }
                } catch (Exception e) {
                    Log.i(ChitFundApplication.TAG, "Error is " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ChitFundApplication.dismissProgress();
                makeToast("Connection Failed", TastyToast.ERROR);
                finish();
            }
        });
    }

    public void updateData(String goldRate) {

        ChitFundApplication.showProgress(this, "");
        ref.child("currentStamp").setValue(ServerValue.TIMESTAMP);
        ref.child("lastStamp").setValue(ServerValue.TIMESTAMP);

        newMonthName = getNextMonth(lastMonth);
        months = months + 1;

        for (int i = 0; i < members; i++) {
            ref.child("members/" + i + "/pending").setValue(pendings.get(i) + getMonthly(goldRate, capacity));
            ref.child("members/" + i + "/monthly/" + newMonthName + "/remain").setValue(pendings.get(i) + getMonthly(goldRate, capacity));
            ref.child("members/" + i + "/monthly/" + newMonthName + "/paid").setValue(0f);
            ref.child("members/" + i + "/monthly/" + newMonthName + "/gift").setValue(0f);
        }

        ref = ref.child("monthly/" + months + "/");
        ref.child("pos").setValue(months);
        ref.child("name").setValue(newMonthName);
        ref.child("default").setValue(true);
        ref.child("takenBy").setValue("none");
        ref.child("takenOn").setValue("none");
        ref.child("bid").setValue("Not required");
        lastAmount = getMonthly(goldRate, capacity);
        ref.child("amount").setValue(getMonthly(goldRate, capacity) + "", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                ChitFundApplication.dismissProgress();
            }
        });

    }

    private float getMonthly(String s, String s1) {

//        float bid = getBid(s, s1, months);
        float f1 = Float.parseFloat(s);
        float f2 = Float.parseFloat(s1);

        float PERCENT = 0.0f;
        if (f1 < 2000000) {
            PERCENT = 3.0f;
        } else {
            PERCENT = 2.0f;
        }

        float f5 = PERCENT * f1 / 100.0F;
//        return (f1 / f2) - ((bid - f5) / f2);
        return f1 / f2;

    }

    private float getMonthly(String s, float bid, String s1) {

        float f1 = Float.parseFloat(s);
        float f2 = Float.parseFloat(s1);

        float PERCENT = 0.0f;
        if (f1 < 2000000) {
            PERCENT = 3.0f;
        } else {
            PERCENT = 2.0f;
        }

        float f5 = PERCENT * f1 / 100.0F;
        return (f1 / f2) - ((bid - f5) / f2);

    }

    private float getBid(String chitAmount, String memberCapacity, long months) {

        float f1 = Float.parseFloat(chitAmount);
        float f2 = Float.parseFloat(memberCapacity);
        float PERCENT = 0.0f;

        if (f1 < 2000000) {
            PERCENT = 3.0f;
        } else {
            PERCENT = 2.0f;
        }

        float f4 = 0.8F * f1 / 100.0F;
        float f5 = PERCENT * f1 / 100.0F;

        return ((f2 - months) * f4) + f5;
    }

    private String getNextMonth(String paramString) {

        if (paramString.toLowerCase().trim().equals("january")) {
            return "February";
        } else if (paramString.toLowerCase().trim().equals("february")) {
            return "March";
        } else if (paramString.toLowerCase().trim().equals("march")) {
            return "April";
        } else if (paramString.toLowerCase().trim().equals("april")) {
            return "May";
        } else if (paramString.toLowerCase().trim().equals("may")) {
            return "June";
        } else if (paramString.toLowerCase().trim().equals("june")) {
            return "July";
        } else if (paramString.toLowerCase().trim().equals("july")) {
            return "August";
        } else if (paramString.toLowerCase().trim().equals("august")) {
            return "September";
        } else if (paramString.toLowerCase().trim().equals("september")) {
            return "October";
        } else if (paramString.toLowerCase().trim().equals("october")) {
            return "November";
        } else if (paramString.toLowerCase().trim().equals("november")) {
            return "December";
        } else return "January";
    }

    private void setupViewPager(ViewPager paramViewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(MonthlyViewFragmentGold.newInstance(id, amount, date, capacity, available), "Monthly");
        adapter.addFragment(MemberViewFragmentGold.newInstance(id, amount, date, capacity, available), "Member");
        paramViewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                String arr[] = {"Add member", "Apply Bid"};
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle("Select One Option");
                ad.setAdapter(new ArrayAdapter<String>(ChitDetailsActivityGold.this, android.R.layout.simple_dropdown_item_1line, arr),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    int avail = Integer.parseInt(available);
                                    int cap = Integer.parseInt(capacity);
                                    if (avail < cap) {
                                        Intent i = new Intent(ChitDetailsActivityGold.this, AddMemberNameActivityGold.class);
                                        i.putExtra("id", id);
                                        i.putExtra("available", available);
                                        i.putExtra("monthly", lastAmount);
                                        i.putExtra("stamp", timeStamp);
                                        i.putExtra("required", cap - avail);
                                        startActivity(i);
                                    } else {
                                        makeToast("Member full", TastyToast.INFO);
                                    }
                                } else {

                                    Intent i = new Intent(ChitDetailsActivityGold.this, ApplyBidActivityGold.class);
                                    i.putExtra("id", id);
                                    i.putExtra("available", available);
                                    i.putExtra("monthly", lastAmount);
                                    i.putExtra("amount", amount);
                                    i.putExtra("capacity", capacity);
                                    startActivity(i);

                                }
                            }
                        });
                ad.show();
                break;
            case R.id.jump_to_date:
                AlertDialog.Builder ad2 = new AlertDialog.Builder(this);
                LayoutInflater inflater = ChitDetailsActivityGold.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.jump_date_dialog, null);
                final AlertDialog alert = ad2.setView(dialogView).create();
                TextView prev, next;
                prev = (TextView) dialogView.findViewById(R.id.prev);
                next = (TextView) dialogView.findViewById(R.id.next);

                prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                        ChitFundApplication.makeToast("Feature not available", TastyToast.ERROR);
                    }
                });
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                        AlertDialog.Builder ad = new AlertDialog.Builder(ChitDetailsActivityGold.this);
                        LayoutInflater inflater = ChitDetailsActivityGold.this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.jump_date_bid_dialog, null);
                        final AlertDialog alert = ad.setView(dialogView).create();
                        final MaterialAutoCompleteTextView name = (MaterialAutoCompleteTextView) dialogView.findViewById(R.id.memberName);
                        ArrayList<String> names = new ArrayList<String>();
                        for (int i = 0; i < MemberViewFragmentGold.list.size(); i++) {
                            names.add(MemberViewFragmentGold.list.get(i).name);
                        }
                        name.setAdapter(new ArrayAdapter<String>(ChitDetailsActivityGold.this, android.R.layout.simple_dropdown_item_1line, names));
                        final MaterialEditText bid = (MaterialEditText) dialogView.findViewById(R.id.bidAmount);
                        Button cancel, done;
                        cancel = (Button) dialogView.findViewById(R.id.cancel);
                        done = (Button) dialogView.findViewById(R.id.done);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alert.dismiss();
                            }
                        });
                        done.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (TextUtils.isEmpty(name.getText())) {
                                    name.setError("Field is empty!");

                                } else if (TextUtils.isEmpty(bid.getText())) {
                                    bid.setError("Field is empty!");
                                } else {
                                    String lastMonth = MonthlyViewFragmentGold.list.get(MonthlyViewFragmentGold.list.size() - 1).name;
                                    String month = getNextMonth(lastMonth);
                                    MonthlyViewFragmentGold.list.add(new ChitDetailsMonthly(month, "" + getMonthly(amount, Float.parseFloat(bid.getText() + ""), capacity), "" + bid.getText(), "user", month, month));
                                    MonthlyViewFragmentGold.adapter.notifyDataSetChanged();
                                    for (int i = 0; i < MemberViewFragmentGold.list.size(); i++) {
                                        if (name.getText().toString().equals(MemberViewFragmentGold.list.get(i).name) && MemberViewFragmentGold.list.get(i).bidMonth.equals("none")) {
                                            ChitDetailsMember item = MemberViewFragmentGold.list.get(i);
                                            item.bidMonth = month + "(" + MonthlyViewFragmentGold.list.size() + ")";
                                            item.bidBy = month + "(" + MonthlyViewFragmentGold.list.size() + ")";
                                            MemberViewFragmentGold.list.remove(i);
                                            MemberViewFragmentGold.list.add(i, item);
                                            MemberViewFragmentGold.adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    alert.dismiss();
                                }
                            }
                        });
                        alert.show();
                    }
                });
                alert.show();
                break;
        }
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
