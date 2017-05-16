package rahulkumardas.chitfund.ui.rent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Iterator;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.chit.ReceiveAmountActivity;

public class ViewRentDetails extends AppCompatActivity {

    private TextView type, no, tenant, amount, advPaid, advPaidDate, rate, years, months;
    private String group, typeS, noS, roomNo;
    private DatabaseReference ref = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private String RENT = ChitFundApplication.RENT;
    ProgressDialog pd;
    private int paymentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rent_details);

        group = getIntent().getStringExtra("name");
        typeS = getIntent().getStringExtra("type");
        noS = getIntent().getStringExtra("no");

        type = (TextView) findViewById(R.id.type);
        type.setText(typeS);
        no = (TextView) findViewById(R.id.no);
        no.setText(noS);
        tenant = (TextView) findViewById(R.id.tenant);
        amount = (TextView) findViewById(R.id.amount);
        advPaid = (TextView) findViewById(R.id.advPaid);
        advPaidDate = (TextView) findViewById(R.id.advPaidDate);
        rate = (TextView) findViewById(R.id.rate);
        months = (TextView) findViewById(R.id.months);
        years = (TextView) findViewById(R.id.years);

        pd = new ProgressDialog(this);
        pd.setMessage("loading...");
        pd.show();
        ref = ref.child(USER + RENT + "/Groups/" + group + "/" + typeS + "/" + noS);
        setStamp();
    }

    private void setStamp() {
        ref.child("slot/currentSnapshot").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            loadData();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private void loadData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("ViewRentDetails", dataSnapshot + "");
                try {
                    long current, prev;
                    tenant.setText(dataSnapshot.child("tenantName").getValue(String.class));
                    amount.setText(dataSnapshot.child("amount").getValue(String.class));
                    roomNo = dataSnapshot.child("RoomNo").getValue(String.class);
                    advPaid.setText(dataSnapshot.child("advPaid").getValue(Float.class) + "");
                    advPaidDate.setText(dataSnapshot.child("advPaidDate").getValue(String.class));
                    rate.setText(dataSnapshot.child("rate").getValue(Float.class) + "");
                    years.setText(dataSnapshot.child("slot/years").getValue(String.class));
                    months.setText(dataSnapshot.child("slot/months").getValue(String.class));
                    current = dataSnapshot.child("slot/currentSnapshot").getValue(Long.class);
                    prev = dataSnapshot.child("slot/lastSnapshot").getValue(Long.class);
                    if (current - prev >= ChitFundApplication.ONE_MONTH_DUR) {

                        float rem = 0f, toPay = Float.valueOf(dataSnapshot.child("amount").getValue(String.class));
                        paymentCount = (int) dataSnapshot.child("slot/months/payments").getChildrenCount();
                        Iterator<DataSnapshot> payments = dataSnapshot.child("slot/months/payments/" + paymentCount).getChildren().iterator();
                        while (payments.hasNext()) {
                            DataSnapshot payment = payments.next();
                            rem = payment.child("remains").getValue(Float.class);
                        }

                        updateData(rem, toPay);
                    }
                    pd.dismiss();
                } catch (Exception e) {
                    Log.i("ViewRentDetails", e.getMessage());
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateData(float rem, float toPay) {
        ref.child("slot/lastSnapshot").setValue(ServerValue.TIMESTAMP);
//        String key = ref.child("slot/payments").push().getKey();
        String key = new Date().getTime() + "";
        ref.child("slot/payments/" + (paymentCount + 1) + "/" + key + "/toPay").setValue(toPay);
        ref.child("slot/payments/" + (paymentCount + 1) + "/" + key + "/remains").setValue((rem + toPay));
        ref.child("slot/payments/" + (paymentCount + 1) + "/" + key + "/paid").setValue(0f);
    }

    public void receive(View view) {
        Intent i = new Intent(this, ReceiveAmountRent.class);
        i.putExtra("name", group);
        i.putExtra("type", typeS);
        i.putExtra("no", noS);
        startActivity(i);
    }

    public void edit(View view) {
        Intent i = new Intent(this, AddNewRentActivity.class);
        i.putExtra("name", group);
        i.putExtra("type", typeS);
        i.putExtra("no", noS);
        i.putExtra("tenantName", tenant.getText().toString());
        i.putExtra("amount", amount.getText().toString());
        i.putExtra("advPaid", advPaid.getText().toString());
        i.putExtra("advDate", advPaidDate.getText().toString());
        i.putExtra("rate", rate.getText().toString());
        i.putExtra("room", roomNo);
        startActivity(i);
    }
}
