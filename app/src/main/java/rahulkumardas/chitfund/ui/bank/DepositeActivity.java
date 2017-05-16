package rahulkumardas.chitfund.ui.bank;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Date;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;

public class DepositeActivity extends AppCompatActivity {

    private MaterialEditText amount, reference;
    private Button depositeNow;
    private ProgressDialog pd;
    private static final String USER = ChitFundApplication.USER;
    private static final String BANK = ChitFundApplication.BANK;
    private DatabaseReference ref = ChitFundApplication.reference;
    private DatabaseReference ref2 = ChitFundApplication.reference;
    private String key;
    private float bal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposite);

        amount = (MaterialEditText)findViewById(R.id.amount);
        reference = (MaterialEditText)findViewById(R.id.reference);
        depositeNow = (Button)findViewById(R.id.depositeNow);

        pd = new ProgressDialog(this);
        pd.setMessage("please wait...");

        key = getIntent().getStringExtra("key");
        bal = getIntent().getFloatExtra("bal", 0f);
        ref = ref.child(USER+BANK+"/"+key);
        ref2 = ref2.child(USER);
        depositeNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(amount.getText())){
                    amount.setError("please enter amount");
                }else if(TextUtils.isEmpty(reference.getText())){
                    reference.setError("please enter reference");
                }else{
                    pd.show();
                    Date date = new Date();
                    bal = bal + (Float.parseFloat(amount.getText().toString()));
                    ref.child("transaction/"+date+"/amount").setValue(Float.parseFloat(amount.getText().toString()));
                    ref.child("transaction/"+date+"/type").setValue("deposit");
                    ref.child("transaction/"+date+"/reference").setValue(reference.getText().toString());
                    String key = ref2.child("sheet").push().getKey();
                    ref2.child("sheet/"+key+"/"+(new Date())).setValue(Float.parseFloat(amount.getText().toString()));
                    ref2.child("sheet/"+key+"/ref").setValue("bank_deposit");
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
