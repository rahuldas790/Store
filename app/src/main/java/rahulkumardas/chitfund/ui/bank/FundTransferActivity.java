package rahulkumardas.chitfund.ui.bank;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;

public class FundTransferActivity extends AppCompatActivity {

    private TextView balance;
    private MaterialEditText amount, chequeNo;
    private MaterialAutoCompleteTextView accountNo;
    private RadioButton online, cheque;
    private Button transfer;
    private float bal;
    private String key;
    private ProgressDialog pd;
    private static final String USER = ChitFundApplication.USER;
    private static final String BANK = ChitFundApplication.BANK;
    private DatabaseReference ref = ChitFundApplication.reference;
    private DatabaseReference ref2 = ChitFundApplication.reference;
    private DatabaseReference ref3 = ChitFundApplication.reference;
    private DatabaseReference ref4 = ChitFundApplication.reference;
    private List<String> accounts;
    private List<Float> balance2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fund_transfer);

        key = getIntent().getStringExtra("key");
        bal = getIntent().getFloatExtra("bal", 0f);

        balance = (TextView) findViewById(R.id.balance);
        balance.setText(bal + " /-");
        accountNo = (MaterialAutoCompleteTextView) findViewById(R.id.accountNo);
        amount = (MaterialEditText) findViewById(R.id.amount);
        chequeNo = (MaterialEditText) findViewById(R.id.chequeNumber);
        chequeNo.setVisibility(View.GONE);
        online = (RadioButton) findViewById(R.id.online);
        cheque = (RadioButton) findViewById(R.id.cheque);
        transfer = (Button) findViewById(R.id.transferNow);

        pd = new ProgressDialog(this);
        pd.setMessage("plese wait...");

        online.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chequeNo.setVisibility(View.VISIBLE);
                if (buttonView.isChecked())
                    chequeNo.setHint("description");
            }
        });
        cheque.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chequeNo.setVisibility(View.VISIBLE);
                if (buttonView.isChecked())
                    chequeNo.setHint("cheque no");
            }
        });

        ref = ref.child(USER + BANK + "/" + key);
        ref2 = ref2.child(USER + BANK);
        ref3 = ref3.child(USER + BANK);
        ref4 = ref4.child(USER);
        pd.show();
        loadData();
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(accountNo.getText())) {
                    accountNo.setError("please enter an amount");
                } else if (TextUtils.isEmpty(amount.getText())) {
                    amount.setError("please enter an amount");
                } else if (bal - Float.parseFloat(amount.getText().toString()) < 0f) {
                    amount.setError("enter an amount less than " + bal);
                } else if (cheque.isChecked() && TextUtils.isEmpty(chequeNo.getText())) {
                    chequeNo.setError("enter the cheque no");
                } else if (online.isChecked() && TextUtils.isEmpty(chequeNo.getText())) {
                    chequeNo.setError("enter transaction description");
                }else if(!accounts.contains(accountNo.getText().toString())){
                    accountNo.setError("please choose account from the dropdown");
                } else {
                    transfer.setClickable(false);
                    pd.show();
                    Date date = new Date();
                    bal = bal - (Float.parseFloat(amount.getText().toString()));
                    ref.child("transaction/" + date + "/amount").setValue(Float.parseFloat(amount.getText().toString()));
                    ref.child("transaction/" + date + "/type").setValue("transfer");
                    if (online.isChecked())
                        ref.child("transaction/" + date + "/mode").setValue("online");
                    else
                        ref.child("transaction/" + date + "/mode").setValue("cheque");
                    ref.child("transaction/" + date + "/description").setValue(chequeNo.getText().toString());
                    ref.child("balance").setValue(bal);

                    ref2 = ref2.child(accountNo.getText().toString());
                    ref2.child("transaction/" + date + "/amount").setValue(Float.parseFloat(amount.getText().toString()));
                    ref2.child("transaction/" + date + "/type").setValue("deposit");
                    ref2.child("transaction/" + date + "/reference").setValue("By transfer");
                    float bal2 = balance2.get(accounts.indexOf(accountNo.getText().toString()));
                    bal2 = bal2 + (Float.parseFloat(amount.getText().toString()));
                    String key = ref4.child("sheet").push().getKey();
                    ref4.child("sheet/"+key+"/"+(new Date())).setValue(Float.parseFloat(amount.getText().toString()));
                    ref4.child("sheet/"+key+"/ref").setValue("bank_to_bank_transfer");
                    ref2.child("balance").setValue(bal2, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            pd.dismiss();
                            transfer.setClickable(true);
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void loadData() {
        ref3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                accounts = new ArrayList<String>();
                balance2 = new ArrayList<Float>();
                Log.i("Rahul", "Snapshot is "+dataSnapshot);
                Iterator<DataSnapshot> accountss = dataSnapshot.getChildren().iterator();
                while(accountss.hasNext()){
                    DataSnapshot account = accountss.next();
                    FundTransferActivity.this.accounts.add(account.getKey());
                    balance2.add(account.child("balance").getValue(Float.class));
                }
                pd.dismiss();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(FundTransferActivity.this, android.R.layout.simple_dropdown_item_1line, accounts);
                accountNo.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
