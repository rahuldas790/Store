package rahulkumardas.chitfund.ui.gold;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.MemberDetailsAdapter;
import rahulkumardas.chitfund.models.MemberDetails;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;

public class MemberDetailsActivityGold extends AppCompatActivity {

    private ListView listView;
    private TextView nameText, amountText, dateText, bidStatusText;
    private DatabaseReference ref = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private final String GOLD = ChitFundApplication.GOLD;
    private String chitId, memberId, chitAmount, date;
    private List<MemberDetails> list;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_details);

        listView = (ListView) findViewById(R.id.listView);
        nameText = (TextView) findViewById(R.id.name);
        amountText = (TextView) findViewById(R.id.amount);
        dateText = (TextView) findViewById(R.id.date);
        bidStatusText = (TextView) findViewById(R.id.bidStatus);

        home = (ImageView) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MemberDetailsActivityGold.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        date = getIntent().getStringExtra("date");
        chitAmount = getIntent().getStringExtra("amount");
        chitId = getIntent().getStringExtra("chitId");
        memberId = getIntent().getStringExtra("memberId");
        ref = ref.child(USER + GOLD + "/Chits/" + chitId + "/members/" + memberId);
        amountText.setText(ChitFundApplication.rupeeFormat(chitAmount));
        dateText.setText(date);
        loadData();
    }

    private void loadData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    list = new ArrayList<MemberDetails>();

                    nameText.setText(dataSnapshot.child("name").getValue(String.class));
                    String bid = dataSnapshot.child("bidMonth").getValue(String.class);
                    String bidBy = dataSnapshot.child("bidBy").getValue(String.class);
                    if (bid.equals("none")) {
                        bidStatusText.setText("Bid not applied");
                    } else {
                        bidStatusText.setText("Bid applied on " + bid + " by " + bidBy);
                    }

                    Iterator<DataSnapshot> months = dataSnapshot.child("monthly").getChildren().iterator();
                    while (months.hasNext()) {
                        DataSnapshot month = months.next();
                        String name = month.getKey();
                        float paid = month.child("paid").getValue(Float.class);
                        float remain = month.child("remain").getValue(Float.class);
                        float gift = month.child("gift").getValue(Float.class);

                        Log.i("Rahul", "paid = " + paid);
                        Log.i("Rahul", "Gift = " + gift);
                        Log.i("Rahul", "Remain = " + remain);

                        MemberDetails item = new MemberDetails(name,  remain, paid, gift);
                        list.add(item);
                    }

                    MemberDetailsAdapter adapter = new MemberDetailsAdapter(MemberDetailsActivityGold.this, list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
