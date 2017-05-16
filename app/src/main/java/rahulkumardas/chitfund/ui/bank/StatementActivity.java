package rahulkumardas.chitfund.ui.bank;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import rahulkumardas.chitfund.adapter.StatementAdapter;
import rahulkumardas.chitfund.models.Statement;
import rahulkumardas.chitfund.ui.ChitFundApplication;

public class StatementActivity extends AppCompatActivity {

    private ProgressDialog pd;
    private float bal;
    private String key;
    private TextView balance;
    private DatabaseReference ref = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private final String BANK = ChitFundApplication.BANK;
    private ListView listView;
    private List<Statement> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement);

        bal = getIntent().getFloatExtra("bal", 0f);
        key = getIntent().getStringExtra("key");

        pd = new ProgressDialog(this);
        pd.setMessage("please wait...");
        pd.show();

        balance = (TextView) findViewById(R.id.balance);
        balance.setText(bal + " /-");
        listView = (ListView)findViewById(R.id.listView);
        ref = ref.child(USER + BANK + "/" + key);
        loadData();
    }

    private void loadData() {
        ref.child("transaction").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list = new ArrayList<Statement>();
                Iterator<DataSnapshot> transactions = dataSnapshot.getChildren().iterator();
                while (transactions.hasNext()) {
                    try {
                        DataSnapshot transaction = transactions.next();
                        String date = transaction.getKey();
                        float amount = transaction.child("amount").getValue(Float.class);
                        String type = transaction.child("type").getValue(String.class);
                        String description, mode;
                        Statement statement = null;
                        if (type.equals("deposit")) {
                            description = transaction.child("reference").getValue(String.class);
                            statement = new Statement(date, description, type, amount);
                        } else if (type.equals("withdraw")) {
                            description = transaction.child("reason").getValue(String.class);
                            statement = new Statement(date, description, type, amount);
                        } else if (type.equals("transfer")) {
                            mode = transaction.child("mode").getValue(String.class);
                            description = transaction.child("description").getValue(String.class);
                            statement = new Statement(date, description, type, mode, amount);
                        }
                        list.add(statement);
                    }catch (Exception e){

                    }

                }

                StatementAdapter adapter = new StatementAdapter(StatementActivity.this, list);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                pd.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
