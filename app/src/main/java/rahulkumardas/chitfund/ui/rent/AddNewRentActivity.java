package rahulkumardas.chitfund.ui.rent;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.gold.AddNewChitActivityGold;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class AddNewRentActivity extends AppCompatActivity {

    private DatabaseReference ref = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private String RENT = ChitFundApplication.RENT;
    private String types[] = {"Flat", "House", "Shop"};
    private String groupName, rentType;
    private MaterialEditText flatEDTXT, roomEDTXT;
    private MaterialAutoCompleteTextView typeEDTXT;
    private Button done;
    private MaterialEditText name, amount, advPaid, advDate, rate;
    private Spinner month, year;
    private String months[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    private String years[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    private long stamp;
    boolean complete = false;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener datePickerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_specification);

        initViews();

        groupName = getIntent().getStringExtra("name");
        Bundle b= getIntent().getExtras();
        if(b.containsKey("tenantName")){
            name.setText(b.getString("tenantName"));
            amount.setText(b.getString("amount"));
            advPaid.setText(b.getString("advPaid"));
            advDate.setText(b.getString("advDate"));
            rate.setText(b.getString("rate"));
            flatEDTXT.setText(b.getString("no"));
            typeEDTXT.setText(b.getString("type"));
            roomEDTXT.setText(b.getString("room"));

            roomEDTXT.setAlpha(0.5f);
            typeEDTXT.setAlpha(0.5f);
            flatEDTXT.setAlpha(0.5f);

            roomEDTXT.setKeyListener(null);
            typeEDTXT.setKeyListener(null);
            flatEDTXT.setKeyListener(null);
        }

//        ChitFundApplication.showProgress(this, "Setting Stamp...");
        ref = ref.child(USER + RENT + "/Groups/" + groupName);
//        setStamp();
        myCalendar = Calendar.getInstance();
        datePickerListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

    }

    private void initViews() {
        typeEDTXT = (MaterialAutoCompleteTextView) findViewById(R.id.type);
        flatEDTXT = (MaterialEditText) findViewById(R.id.flatNo);
        roomEDTXT = (MaterialEditText) findViewById(R.id.roomNo);

        name = (MaterialEditText) findViewById(R.id.name);
        amount = (MaterialEditText) findViewById(R.id.amount);
        advPaid = (MaterialEditText) findViewById(R.id.advPaid);
        advDate = (MaterialEditText) findViewById(R.id.advDate);
        rate = (MaterialEditText) findViewById(R.id.rate);
        year = (Spinner) findViewById(R.id.year);
        month = (Spinner) findViewById(R.id.month);

        typeEDTXT.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, types));

        done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFields()) {
                    new AddNewRent().execute();
                }
            }
        });

        advDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddNewRentActivity.this, datePickerListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        year.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, years));
        month.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, months));
    }

    private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        String dateStr = sdf.format(myCalendar.getTime());
        dateStr = dateStr.toLowerCase().trim();
        if(dateStr.contains("a")||dateStr.contains("e")||dateStr.contains("i")||dateStr.contains("o")||dateStr.contains("u")){
            advDate.setText(dateStr);
        }else{
            advDate.setText(dateStr);
        }
    }

    public class AddNewRent extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(AddNewRentActivity.this, "Adding new ent...");
        }

        @Override
        protected Object doInBackground(Object[] params) {
            String type = typeEDTXT.getText().toString();
            String flatNo = flatEDTXT.getText().toString();
            String roomNo = roomEDTXT.getText().toString();
            float required = Float.parseFloat(amount.getText().toString());
            float paid = Float.parseFloat(advPaid.getText().toString());
            ref.child(type + "/" + flatNo + "/RoomNo").setValue(roomNo);
            ref.child(type + "/" + flatNo + "/tenantName").setValue(name.getText().toString());
            ref.child(type + "/" + flatNo + "/amount").setValue(required + "");
            ref.child(type + "/" + flatNo + "/advPaid").setValue(paid);
            ref.child(type + "/" + flatNo + "/pending").setValue(required - paid);
            ref.child(type + "/" + flatNo + "/advPaidDate").setValue(advDate.getText().toString());
            ref.child(type + "/" + flatNo + "/rate").setValue(Float.parseFloat(rate.getText().toString()));
            int months = month.getSelectedItemPosition();
            int years = year.getSelectedItemPosition();
            long tot_dur = (years * 12l + months) * ChitFundApplication.ONE_MONTH_DUR;
            ref.child(type + "/" + flatNo + "/slot/totDur").setValue(tot_dur);
//            ref.child(type + "/" + flatNo + "/slot/lastSnapshot").setValue(0);

            ref.child(type + "/" + flatNo + "/slot/currentSnapshot").setValue(ServerValue.TIMESTAMP);
            ref.child(type + "/" + flatNo + "/slot/lastSnapshot").setValue(ServerValue.TIMESTAMP);
            long time = new Date().getTime();
            ref.child(type + "/" + flatNo + "/slot/payments/1/" + time + "/toPay").setValue(required);
            ref.child(type + "/" + flatNo + "/slot/payments/1/" + time + "/paid").setValue(paid);
            ref.child(type + "/" + flatNo + "/slot/payments/1/" + time + "/remains").setValue(required - paid);
            ref.child(type + "/" + flatNo + "/slot/months").setValue(months + "");
            ref.child(type + "/" + flatNo + "/slot/years").setValue(years + "", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    complete = true;
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            makeToast("Rent Success", TastyToast.SUCCESS);
            dismissProgress();
            finish();

        }
    }

    private boolean verifyFields() {

        if (TextUtils.isEmpty(typeEDTXT.getText())) {
            typeEDTXT.setError("Enter rent type");
            return false;
        } else if (TextUtils.isEmpty(flatEDTXT.getText())) {
            rentType = typeEDTXT.getText().toString();
            flatEDTXT.setError("Enter " + rentType + " no");
            return false;
        } else if (TextUtils.isEmpty(roomEDTXT.getText())) {
            roomEDTXT.setError("Enter room no");
            return false;
        } else if (TextUtils.isEmpty(name.getText())) {
            name.setError("Enter the name");
            return false;
        } else if (TextUtils.isEmpty(amount.getText())) {
            amount.setError("Enter the amount");
            return false;
        } else if (TextUtils.isEmpty(advPaid.getText())) {
            advPaid.setError("Enter advanced paid");
            return false;
        } else if (TextUtils.isEmpty(advDate.getText())) {
            advDate.setError("Enter advanced paid date");
            return false;
        } else if (TextUtils.isEmpty(rate.getText())) {
            rate.setError("Enter interest rate");
            return false;
        }
        return true;
    }

    public void setStamp() {
        ref.child("/stamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                ChitFundApplication.dismissProgress();
            }
        });
    }

}
