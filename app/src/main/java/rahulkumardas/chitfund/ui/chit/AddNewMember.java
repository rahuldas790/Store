package rahulkumardas.chitfund.ui.chit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.Iterator;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;

public class AddNewMember extends AppCompatActivity {

    MaterialEditText name, phone, village, email, address, shop, related;
    DatabaseReference ref = ChitFundApplication.reference;
    private final String EMAIL = "\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
    private final String USER = ChitFundApplication.USER;
    ArrayList<String> names;
    ArrayList<String> phones;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_member);

        home = (ImageView)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddNewMember.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        name = (MaterialEditText) findViewById(R.id.name);
        phone = (MaterialEditText) findViewById(R.id.phone);
        email = (MaterialEditText) findViewById(R.id.email);
        email.validateWith(new RegexpValidator("Enter a valid email", EMAIL));
        village = (MaterialEditText) findViewById(R.id.village);
        address = (MaterialEditText) findViewById(R.id.address);
        shop = (MaterialEditText) findViewById(R.id.shop);
        related = (MaterialEditText) findViewById(R.id.related);

        ChitFundApplication.showProgress(this, "");
        ref = ref.child(USER+"/Members");
        getCount();

    }

    private void getCount() {
        names = new ArrayList();
        phones = new ArrayList();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterator<DataSnapshot> members = dataSnapshot.getChildren().iterator();
                while(members.hasNext()){
                    DataSnapshot member = members.next();
                    names.add(member.child("name").getValue(String.class).toLowerCase());
                    phones.add(member.child("phone").getValue(String.class));
                }
                ChitFundApplication.dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void submit(View view) {
        Log.i(ChitFundApplication.TAG,"Inside onClick");
        if(TextUtils.isEmpty(name.getText())||TextUtils.isEmpty(phone.getText())||TextUtils.isEmpty(email.getText())||TextUtils.isEmpty(shop.getText())||TextUtils.isEmpty(village.getText())||
                TextUtils.isEmpty(address.getText())||TextUtils.isEmpty(related.getText())){
            ChitFundApplication.makeToast("Empty Field(s)...", TastyToast.INFO);
        }else if(names.contains(name.getText().toString().toLowerCase())){
            ChitFundApplication.makeToast("Name already exist!", TastyToast.WARNING);
        }else if(phones.contains(phone.getText().toString())){
            ChitFundApplication.makeToast("Phone no. already exist!", TastyToast.WARNING);
        }else{
            String key = ref.push().getKey();
            ref = ref.child("/"+key+"/");
            ChitFundApplication.showProgress(this, "Adding member..");
            ref.child("name").setValue(name.getText().toString());
            ref.child("phone").setValue(phone.getText().toString());
            ref.child("email").setValue(email.getText().toString());
            ref.child("village").setValue(village.getText().toString());
            ref.child("address").setValue(address.getText().toString());
            ref.child("shop").setValue(shop.getText().toString());
            ref.child("related").setValue(related.getText().toString());
            ref.child("id").setValue("id", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    finish();
                    ChitFundApplication.makeToast("Successful", TastyToast.SUCCESS);
//                    ChitFundApplication.dismissProgress();
                }
            });
        }
    }
}
