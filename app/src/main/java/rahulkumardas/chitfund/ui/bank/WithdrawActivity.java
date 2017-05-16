package rahulkumardas.chitfund.ui.bank;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Date;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;

public class WithdrawActivity extends AppCompatActivity {

    private TextView balance;
    MaterialEditText amount, reason;
    private Button withdraw;
    private ProgressDialog pd;
    private float bal;
    private String key;
    private DatabaseReference ref = ChitFundApplication.reference;
    private DatabaseReference ref2 = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private final String BANK = ChitFundApplication.BANK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        bal = getIntent().getFloatExtra("bal", 0f);
        key = getIntent().getStringExtra("key");

        pd = new ProgressDialog(this);
        pd.setMessage("please wait...");

        balance = (TextView)findViewById(R.id.balance);
        balance.setText(bal+" /-");
        amount = (MaterialEditText) findViewById(R.id.amount);
        reason = (MaterialEditText)findViewById(R.id.reason);
        withdraw = (Button)findViewById(R.id.withdrawNow);
        ref = ref.child(USER+BANK+"/"+key);
        ref2 = ref2.child(USER);
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(amount.getText())){
                    amount.setError("please enter an amount");
                }else if(bal - Float.parseFloat(amount.getText().toString()) < 0f){
                    amount.setError("enter an amount less than "+bal);
                }else if(TextUtils.isEmpty(reason.getText())){
                    reason.setError("enter the reaso to withdraw");
                }else{
                    pd.show();
                    Date date = new Date();
                    bal = bal - (Float.parseFloat(amount.getText().toString()));
                    ref.child("transaction/"+date+"/amount").setValue(Float.parseFloat(amount.getText().toString()));
                    ref.child("transaction/"+date+"/type").setValue("withdraw");
                    ref.child("transaction/"+date+"/reason").setValue(reason.getText().toString());
                    String key = ref2.child("sheet").push().getKey();
                    ref2.child("sheet/"+key+"/"+(new Date())).setValue(Float.parseFloat(amount.getText().toString()));
                    ref2.child("sheet/"+key+"/ref").setValue("bank_withdraw");
                    ref.child("balance").setValue(bal, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            pd.dismiss();
                            finish();
                        }
                    });
                }
            }
        });
    }
}
