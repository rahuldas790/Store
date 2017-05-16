package rahulkumardas.chitfund.ui.rent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.ReceiveAmountRentAdapter;
import rahulkumardas.chitfund.models.ReceiveModelRent;
import rahulkumardas.chitfund.ui.ChitFundApplication;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class ReceiveAmountRent extends AppCompatActivity {

    private String groupName, type, no;
    private DatabaseReference ref = ChitFundApplication.reference;
    private DatabaseReference ref2 = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private final String RENT = ChitFundApplication.RENT;
    private static List<ReceiveModelRent> list;
    private ProgressBar bar;
    private LinearLayout layout;
    private static ListView listView;
    private TextView totalAmount, toPay, remains;
    MaterialEditText receivingAmount;
    private Button calculate, receive;
    float amt = 0;
    private int paymentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_amount_rent);

        groupName = getIntent().getStringExtra("name");
        type = getIntent().getStringExtra("type");
        no = getIntent().getStringExtra("no");
        Toast.makeText(this, groupName + " " + type + " " + no, Toast.LENGTH_SHORT).show();
        ref = ref.child(USER + RENT + "/Groups/" + groupName + "/" + type + "/" + no + "/slot/payments");
        ref2 = ref2.child(USER);
        listView = (ListView) findViewById(R.id.recyclerView);

        bar = (ProgressBar) findViewById(R.id.bar);
        layout = (LinearLayout) findViewById(R.id.content);
        totalAmount = (TextView) findViewById(R.id.totalPending);
        calculate = (Button) findViewById(R.id.calculate);
        toPay = (TextView) findViewById(R.id.totalAmount);
        remains = (TextView) findViewById(R.id.remains);
        receive = (Button) findViewById(R.id.receive);
        receivingAmount = (MaterialEditText) findViewById(R.id.receivingAmount);


        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long date = new Date().getTime();
                float amount = Float.parseFloat(receivingAmount.getText().toString());
                new ReceiveAmount().execute(date + "", amount + "");

            }
        });

        getDetails();
    }

    private void getDetails() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Toast.makeText(ReceiveAmountRent.this, "" + dataSnapshot, Toast.LENGTH_SHORT).show();
                    Log.i("Data Snapshot", dataSnapshot + "");
                    list = new ArrayList<ReceiveModelRent>();
                    paymentCount = (int) dataSnapshot.getChildrenCount();
                    Iterator<DataSnapshot> payments = dataSnapshot.getChildren().iterator();
                    while (payments.hasNext()) {
                        DataSnapshot payment = payments.next();
                        // get the sub payments in a month
                        Iterator<DataSnapshot> subs = payment.getChildren().iterator();
                        while (subs.hasNext()) {
                            DataSnapshot sub = subs.next();
                            String date = sub.getKey();
                            float amount = sub.child("paid").getValue(Float.class);
                            float remains = sub.child("remains").getValue(Float.class);
                            amt = remains;
                            list.add(new ReceiveModelRent(convertTime(Long.valueOf(date)), amount, remains));
                        }
                    }
                    ReceiveAmountRentAdapter adapter = new ReceiveAmountRentAdapter(ReceiveAmountRent.this, list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    totalAmount.setText(amt + "");
                    bar.setVisibility(View.GONE);
                } catch (Exception e) {
                    Toast.makeText(ReceiveAmountRent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class ReceiveAmount extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(ReceiveAmountRent.this, "");
        }

        @Override
        protected String doInBackground(String[] params) {
            long time = Long.parseLong(params[0]);
            float amount = Float.parseFloat(params[1]);
            float remain = amt - amount;
            ref.child(paymentCount + "/" + time + "/paid").setValue(amount);
            ref.child(paymentCount + "/" + time + "/remains").setValue(remain);
            String key = ref2.child("sheet").push().getKey();
            ref2.child("sheet/" + key + "/" + (new Date())).setValue(amount);
            ref2.child("sheet/" + key + "/ref").setValue("rent");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dismissProgress();
            makeToast("Amount Received", TastyToast.SUCCESS);
            finish();
        }
    }

    private String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd/mm/yyyy");
        return format.format(date);
    }
}
