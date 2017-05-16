package rahulkumardas.chitfund.ui.chit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.RestAdapterAPI;
import rahulkumardas.chitfund.models.ChitDetailsMember;
import rahulkumardas.chitfund.models.ChitDetailsMonthly;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;
import static rahulkumardas.chitfund.ui.ChitFundApplication.rupeeFormat;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class ChitDetailsActivity extends AppCompatActivity implements View.OnClickListener {


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String id, amount, date, capacity;
    public static String available;
    private DatabaseReference ref = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private long months = 0;
    private String lastMonth, newMonthName;
    private float lastAmount;
    private long members = 0, timeStamp;
    private ArrayList<Float> pendings;
    private ImageButton add;
    private boolean updateDone = false;
    private Thread thread;
    private ImageView home, jumpTo;
    private boolean success;

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
                Intent i = new Intent(ChitDetailsActivity.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        ref = ref.child(USER + "/Chits/" + id + "/");

        setTimeStamp();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
    }


    private void setTimeStamp() {
        ref.child("currentStamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError == null) {
                    success = true;
                    ChitFundApplication.showProgress(ChitDetailsActivity.this);
                    checkUpdate();
//                    new CheckUpdate().execute();
                } else {
                    makeToast("Connection Failed", TastyToast.ERROR);
                    success = false;
                }
            }
        });
    }

    private void checkUpdate() {
        RestAdapterAPI adapterAPI = ChitFundApplication.getAdapter();

        Call<JsonObject> result = adapterAPI.checkUpdate(id);
        result.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject object = response.body();
                long lastStamp = object.get("lastStamp").getAsLong();
                long currentStamp = object.get("currentStamp").getAsLong();
                timeStamp = currentStamp;
                ChitFundApplication.dismissProgress();
                final long ONE_MONTH_DUR = 2629746000L;
                final long TEN_MIN_DUR = 600000L;
                if (currentStamp - lastStamp >= TEN_MIN_DUR && !updateDone) {

                    months = object.get("monthly").getAsJsonArray().size()-1;
                    members = object.get("members").getAsJsonArray().size();

                    JsonArray members = object.get("members").getAsJsonArray();
                    available = String.valueOf(members.size());
                    pendings = new ArrayList<Float>();
                    JsonObject member = null;
                    for(int i=0;i<members.size();i++){
                        try {
                            member = members.get(i).getAsJsonObject();
                            pendings.add(member.get("pending").getAsFloat());
                        }catch (Exception e){

                        }
                    }

                    JsonArray monthly = object.get("monthly").getAsJsonArray();
                    int size = monthly.size();
                    JsonObject lastMonthly = monthly.get(size-1).getAsJsonObject();
                    lastMonth = lastMonthly.get("name").getAsString();
                    lastAmount = Float.parseFloat(lastMonthly.get("amount").getAsString());

                    new AlertDialog.Builder(ChitDetailsActivity.this)
                            .setTitle("Update!")
                            .setMessage("Hi,\nIts one month past the last update. Do you want to update now?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateDone = true;
                                    success = true;
                                    new UpdateData().execute();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ChitFundApplication.dismissProgress();
                                    finish();
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    JsonArray monthly = object.get("monthly").getAsJsonArray();
                    int size = monthly.size();
                    JsonObject lastMonthly = monthly.get(size-1).getAsJsonObject();
                    lastMonth = lastMonthly.get("name").getAsString();
                    lastAmount = Float.parseFloat(lastMonthly.get("amount").getAsString());
                    months = monthly.size()-1;
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    public class CheckUpdate extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ChitFundApplication.showProgress(ChitDetailsActivity.this);
            success = false;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        long lastStamp = dataSnapshot.child("lastStamp").getValue(Long.class);
                        long currentStamp = dataSnapshot.child("currentStamp").getValue(Long.class);
                        timeStamp = currentStamp;
                        ChitFundApplication.dismissProgress();
                        final long ONE_MONTH_DUR = 2629746000L;
                        final long TEN_MIN_DUR = 600000L;
                        if (currentStamp - lastStamp >= TEN_MIN_DUR && !updateDone) {

                            months = dataSnapshot.child("monthly").getChildrenCount();
                            members = dataSnapshot.child("members").getChildrenCount();
                            lastMonth = dataSnapshot.child("monthly/" + months + "/name").getValue(String.class);
                            lastAmount = Float.parseFloat(dataSnapshot.child("monthly/" + months + "/amount").getValue(String.class));
                            Iterator<DataSnapshot> members = dataSnapshot.child("members").getChildren().iterator();
                            available = String.valueOf(dataSnapshot.child("members").getChildrenCount());
                            pendings = new ArrayList<Float>();
                            DataSnapshot member = null;
                            while (members.hasNext()) {
                                member = members.next();
                                pendings.add(member.child("pending").getValue(Float.class));
                            }

                            new AlertDialog.Builder(ChitDetailsActivity.this)
                                    .setTitle("Update!")
                                    .setMessage("Hi,\nIts one month past the last update. Do you want to update now?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            updateDone = true;
                                            success = true;
                                            new UpdateData().execute();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ChitFundApplication.dismissProgress();
                                            finish();
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

                }
            });
            return null;
        }
    }

    public class UpdateData extends AsyncTask {
        @Override
        protected void onPreExecute() {
            showProgress(ChitDetailsActivity.this, "updating..");
            super.onPreExecute();
            success = false;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            ref.child("currentStamp").setValue(ServerValue.TIMESTAMP);
            ref.child("lastStamp").setValue(ServerValue.TIMESTAMP);

            newMonthName = getNextMonth(lastMonth);
            months = months + 1;

            Map<String, Object> map = new HashMap<>();

            for (int i = 0; i < members; i++) {
                map.clear();
                ref.child("members/" + i + "/pending").setValue(pendings.get(i) + getMonthly(amount, capacity));

                map.put("remain", pendings.get(i) + getMonthly(amount, capacity));
                map.put("paid", 0f);
                map.put("gift", 0f);
                ref.child("members/" + i + "/monthly/" + newMonthName).setValue(map);
            }

            ref = ref.child("monthly/" + months + "/");

            map.clear();

            map.put("pos", months);
            map.put("name", newMonthName);
            map.put("default", true);
            map.put("takenBy", "none");
            map.put("takenOn", "none");
            map.put("bid", getBid(amount, capacity, months) + "");
            lastAmount = getMonthly(amount, capacity);
            map.put("amount", getMonthly(amount, capacity)+"");

            ref.setValue(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    success = true;
                }
            });
            return null;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            dismissProgress();
        }
    }

    private float getMonthly(String s, String s1) {

        float bid = getBid(s, s1, months);
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
        adapter.addFragment(MonthlyViewFragment.newInstance(id, rupeeFormat(amount), date, capacity, available), "Monthly");
        adapter.addFragment(MemberViewFragment.newInstance(id, rupeeFormat(amount), date, capacity, available), "Member");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                String arr[] = {"Add member", "Apply Bid"};
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle("Select One Option");
                ad.setAdapter(new ArrayAdapter<String>(ChitDetailsActivity.this, android.R.layout.simple_dropdown_item_1line, arr),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    int avail = Integer.parseInt(available);
                                    int cap = Integer.parseInt(capacity);
                                    if (avail < cap) {
                                        Intent i = new Intent(ChitDetailsActivity.this, AddMemberNameActivity.class);
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

                                    if(MemberViewFragment.checkBidMember()) {
                                        Intent i = new Intent(ChitDetailsActivity.this, ApplyBidActivity.class);
                                        i.putExtra("id", id);
                                        i.putExtra("available", available);
                                        i.putExtra("monthly", lastAmount);
                                        i.putExtra("amount", amount);
                                        i.putExtra("capacity", capacity);
                                        startActivity(i);
                                    }else{
                                        Toast.makeText(ChitDetailsActivity.this, "No member to apply bid", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        });
                ad.show();
                break;
            case R.id.jump_to_date:
                AlertDialog.Builder ad2 = new AlertDialog.Builder(this);
                LayoutInflater inflater = ChitDetailsActivity.this.getLayoutInflater();
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
                        AlertDialog.Builder ad = new AlertDialog.Builder(ChitDetailsActivity.this);
                        LayoutInflater inflater = ChitDetailsActivity.this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.jump_date_bid_dialog, null);
                        final AlertDialog alert = ad.setView(dialogView).create();
                        final MaterialAutoCompleteTextView name = (MaterialAutoCompleteTextView) dialogView.findViewById(R.id.memberName);
                        ArrayList<String> names = new ArrayList<String>();
                        for (int i = 0; i < MemberViewFragment.list.size(); i++) {
                            names.add(MemberViewFragment.list.get(i).name);
                        }
                        name.setAdapter(new ArrayAdapter<String>(ChitDetailsActivity.this, android.R.layout.simple_dropdown_item_1line, names));
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
                                    String lastMonth = MonthlyViewFragment.list.get(MonthlyViewFragment.list.size() - 1).name;
                                    String month = getNextMonth(lastMonth);
                                    MonthlyViewFragment.list.add(new ChitDetailsMonthly(month, "" + getMonthly(amount, Float.parseFloat(bid.getText() + ""), capacity), "" + bid.getText(), "user", month, month));
                                    MonthlyViewFragment.adapter.notifyDataSetChanged();
                                    for (int i = 0; i < MemberViewFragment.list.size(); i++) {
                                        if (name.getText().toString().equals(MemberViewFragment.list.get(i).name) && MemberViewFragment.list.get(i).bidMonth.equals("none")) {
                                            ChitDetailsMember item = MemberViewFragment.list.get(i);
                                            item.bidMonth = month + "(" + MonthlyViewFragment.list.size() + ")";
                                            item.bidBy = month + "(" + MonthlyViewFragment.list.size() + ")";
                                            MemberViewFragment.list.remove(i);
                                            MemberViewFragment.list.add(i, item);
                                            MemberViewFragment.adapter.notifyDataSetChanged();
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
