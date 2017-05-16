package rahulkumardas.chitfund.ui.gold;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.HomeActivity;

import static rahulkumardas.chitfund.ui.ChitFundApplication.GOLD_COMM;

public class AddNewChitActivityGold extends AppCompatActivity implements View.OnClickListener{

    MaterialEditText amount,date, capacity, months, goldRate;
    TextView submit;
    private DatabaseReference ref = ChitFundApplication.reference;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener datePickerListener;
    private final String USER = ChitFundApplication.USER;
    private final String GOLD = ChitFundApplication.GOLD;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_chit_gold);

        amount = (MaterialEditText) findViewById(R.id.total_amount);
        goldRate = (MaterialEditText) findViewById(R.id.gold_rate);
        date = (MaterialEditText) findViewById(R.id.starting_date);
        capacity = (MaterialEditText) findViewById(R.id.member_capacity);
        months = (MaterialEditText) findViewById(R.id.no_of_months);
        months.setKeyListener(null);
        submit = (TextView) findViewById(R.id.submit);
        submit.setOnClickListener(this);
        home = (ImageView)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddNewChitActivityGold.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

//        amount.validateWith(new RegexpValidator("Enter a valid amount!", "\\d+"));
//        capacity.validateWith(new RegexpValidator("Only Integer allowed!", "\\d+"));


        ref = ref.child(USER+GOLD+"/Chits");
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

        date.setOnClickListener(this);
        capacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(capacity.getText())){
                    months.setText(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        String dateStr = sdf.format(myCalendar.getTime());
        dateStr = dateStr.toLowerCase().trim();
        if(dateStr.contains("a")||dateStr.contains("e")||dateStr.contains("i")||dateStr.contains("o")||dateStr.contains("u")){
            date.setText(dateStr);
        }else{
            date.setText(dateStr);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.submit:
                if (TextUtils.isEmpty(amount.getText()) || TextUtils.isEmpty(date.getText()) || TextUtils.isEmpty(capacity.getText())||TextUtils.isEmpty(goldRate.getText())) {
                    ChitFundApplication.makeToast("Field(s) filled", TastyToast.INFO);
                } else addNewChit();
                break;
            case R.id.starting_date:
//                showDialog(R.id.starting_date);
                new DatePickerDialog(AddNewChitActivityGold.this, datePickerListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }

    private void addNewChit() {
        int amount = Integer.parseInt(this.amount.getText().toString())+GOLD_COMM;
        Log.i(ChitFundApplication.TAG, "Submit Initiated");
        ChitFundApplication.showProgress(this, "Adding chit...");
        String key = ref.push().getKey();
        ref = ref.child("/"+key+"/");
        ref.child("id").setValue("1");
        ref.child("active").setValue(true);
        ref.child("amount").setValue(amount+"");
        ref.child("rate").setValue(goldRate.getText().toString());
        ref.child("date").setValue(date.getText().toString());
        ref.child("capacity").setValue(capacity.getText().toString());
        ref.child("month").setValue(capacity.getText().toString());
        ref.child("toPay").setValue(""+getMonthly(amount, capacity.getText().toString()));
        ref.child("monthly/1/pos").setValue("1");
        ref.child("monthly/1/name").setValue(getMonth(date.getText().toString()));
        ref.child("monthly/1/amount").setValue(""+getMonthly(amount, capacity.getText().toString()));
        ref.child("monthly/1/bid").setValue("Not required");
        ref.child("monthly/1/default").setValue(true);
        ref.child("monthly/1/takenBy").setValue("none");
        ref.child("monthly/1/takenOn").setValue("none");
        ref.child("lastStamp").setValue(myCalendar.getTimeInMillis());
        ref.child("available").setValue("0", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                ChitFundApplication.makeToast("Successful..", TastyToast.SUCCESS);
                ChitFundApplication.dismissProgress();
                finish();
            }
        });
    }

    private float getMonthly(int amount, String s1) {

//        float bid = getBid(amount, s1);
        float f1 = amount;
        float f2 = Float.parseFloat(s1);

        float PERCENT = 0.0f;
        if(f1<2000000){
            PERCENT = 3.0f;
        }else{
            PERCENT = 2.0f;
        }

        float f5 = PERCENT * f1 / 100.0F;
//        return (f1/f2)-((bid-f5)/f2);
        return f1/f2;
    }

    private float getBid(String chitAmount, String memberCapacity)
    {

        float f1 = Float.parseFloat(chitAmount);
        float f2 = Float.parseFloat(memberCapacity);
        float PERCENT = 0.0f;

        if(f1<2000000){
            PERCENT = 3.0f;
        }else{
            PERCENT = 2.0f;
        }

        float f4 = 0.8F * f1 / 100.0F;
        float f5 = PERCENT * f1 / 100.0F;

        return ((f2-1) * f4) + f5;
    }

    private String getMonth(String text) {
//        String val[] = text.split("//");
        int no = Integer.parseInt(text.substring(0,2));
        switch(no){
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default:
                return "blah";
        }
    }
}
