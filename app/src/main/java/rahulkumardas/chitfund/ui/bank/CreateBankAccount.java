package rahulkumardas.chitfund.ui.bank;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;

public class CreateBankAccount extends AppCompatActivity {

    private MaterialEditText account, name, bankName, ifsc, branch, address;
    private final String USER = ChitFundApplication.USER;
    private static final String BANK = ChitFundApplication.BANK;
    private DatabaseReference ref = ChitFundApplication.reference;
    private Button create;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bank_account);

        account = (MaterialEditText)findViewById(R.id.accountNo);
        name = (MaterialEditText)findViewById(R.id.name);
        bankName = (MaterialEditText)findViewById(R.id.bankName);
        ifsc = (MaterialEditText)findViewById(R.id.ifscCode);
        branch = (MaterialEditText)findViewById(R.id.branch);
        address = (MaterialEditText)findViewById(R.id.address);
        create = (Button)findViewById(R.id.createNow);
        pd = new ProgressDialog(this);
        pd.setMessage("Createing...");

        ref = ref.child(USER+BANK);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();

                if(TextUtils.isEmpty(account.getText())){
                    account.setError("Enter account number");
                }else if(TextUtils.isEmpty(name.getText())){
                    name.setError("Enter member name");
                }else if(TextUtils.isEmpty(bankName.getText())){
                    bankName.setError("Enter bank name");
                }else if(TextUtils.isEmpty(ifsc.getText())){
                    ifsc.setError("Enter ifsc code");
                }else if(TextUtils.isEmpty(branch.getText())){
                    branch.setError("Enter branch name");
                }else if(TextUtils.isEmpty(address.getText())){
                    address.setError("Enter address");
                }else{
                    String root = bankName.getText().toString()+" "+account.getText().toString();
                    ref.child(root+"/accountNo").setValue(account.getText().toString());
                    ref.child(root+"/name").setValue(name.getText().toString());
                    ref.child(root+"/bankName").setValue(bankName.getText().toString());
                    ref.child(root+"/ifsc").setValue(ifsc.getText().toString());
                    ref.child(root+"/branch").setValue(branch.getText().toString());
                    ref.child(root+"/address").setValue(address.getText().toString(), new DatabaseReference.CompletionListener() {
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
