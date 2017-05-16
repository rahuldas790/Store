package rahulkumardas.chitfund.ui.bank;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;

public class ViewBankActivity extends AppCompatActivity implements View.OnClickListener {

    private Button deposite, statement, transfer, withdraw;
    private TextView balance, title;
    private String key;
    private ProgressDialog pd;
    private DatabaseReference ref = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private final String BANK = ChitFundApplication.BANK;
    private float bal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bank);

        key = getIntent().getStringExtra("key");

        deposite = (Button) findViewById(R.id.deposite);
        statement = (Button) findViewById(R.id.statement);
        transfer = (Button) findViewById(R.id.transfer);
        withdraw = (Button) findViewById(R.id.withdraw);
        balance = (TextView) findViewById(R.id.balance);
        title = (TextView) findViewById(R.id.title);
        title.setText(key);

        deposite.setOnClickListener(this);
        statement.setOnClickListener(this);
        transfer.setOnClickListener(this);
        withdraw.setOnClickListener(this);

        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.show();
        ref = ref.child(USER + BANK + "/" + key);
        loadData();
    }

    private void loadData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    bal = dataSnapshot.child("/balance").getValue(Float.class);
                    balance.setText(bal + " /-");
                } catch (Exception e) {

                }
                pd.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        Intent i;
        switch (id) {
            case R.id.deposite:
                i = new Intent(this, DepositeActivity.class);
                i.putExtra("key", key);
                i.putExtra("bal", bal);
                startActivity(i);
                break;
            case R.id.withdraw:
                i = new Intent(this, WithdrawActivity.class);
                i.putExtra("key", key);
                i.putExtra("bal", bal);
                startActivity(i);
                break;
            case R.id.transfer:
                i = new Intent(this, FundTransferActivity.class);
                i.putExtra("key", key);
                i.putExtra("bal", bal);
                startActivity(i);
                break;
            case R.id.statement:
                i = new Intent(this, StatementActivity.class);
                i.putExtra("key", key);
                i.putExtra("bal", bal);
                startActivity(i);
                break;
        }
    }
}
