package rahulkumardas.chitfund.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

public class BalanceSheetActivity extends AppCompatActivity {

    private DatabaseReference ref = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;
    private List<Sheet> list;
    private LinearLayout layout;
    private Button download;
    private final String DATABASE_NAME = "user";
    private final String TABLE_NAME = "BSheet";
    private final int DATABASE_VERSION = 1;
    private OpenHelper databse;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_sheet);

        layout = (LinearLayout) findViewById(R.id.activity_balance_sheet);
        download = (Button) findViewById(R.id.download);
        ref = ref.child(USER);
        showProgress(this, "preparing sheet...");
        loadData();

        databse = new OpenHelper(this);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                databse.dropTable();
                for (int i = 0; i < list.size(); i++) {
                    databse.addEntry(list.get(i));
                }

                exportDB(databse);
                Toast.makeText(BalanceSheetActivity.this, "Database saved to sdcard/ChitFund/database.csv", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list = new ArrayList<Sheet>();
                layout.removeAllViews();

                if (dataSnapshot.child("sheet").exists()) {
                    Iterator<DataSnapshot> sheets = dataSnapshot.child("sheet").getChildren().iterator();
                    while (sheets.hasNext()) {
                        DataSnapshot sheet = sheets.next();
                        Iterator<DataSnapshot> datas = sheet.getChildren().iterator();
                        DataSnapshot data = datas.next();
                        String date = data.getKey();
                        float amount = 0f;
                        amount = data.getValue(Float.class);
                        String ref = sheet.child("ref").getValue(String.class);

                        Sheet sheet1 = new Sheet(amount, date, ref);
                        View view = LayoutInflater.from(BalanceSheetActivity.this).inflate(R.layout.item_balance_sheet, null);
                        TextView dateView = (TextView) view.findViewById(R.id.date);
                        TextView amountView = (TextView) view.findViewById(R.id.amount);
                        TextView refrenceView = (TextView) view.findViewById(R.id.reference);
                        dateView.setText(date);
                        amountView.setText(amount + "");
                        refrenceView.setText(ref);
                        layout.addView(view);
                        list.add(sheet1);
                    }
                }

                if (list.isEmpty()) {
                    download.setVisibility(View.GONE);
                    TextView tv = new TextView(BalanceSheetActivity.this);
                    tv.setText("No Record Found!");
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    param.gravity = Gravity.CENTER;
                    tv.setLayoutParams(param);
                    layout.addView(tv);
                } else {
                    download.setVisibility(View.VISIBLE);
                }

                dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class Sheet {
        public Sheet(float amount, String date, String ref) {
            this.amount = amount + "";
            this.date = date;
            this.ref = ref;
        }

        public String date, ref;
        public String amount;

    }

    private class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context) {
            super(context, Environment.getExternalStorageDirectory()
                    + File.separator + "ChitFund"
                    + File.separator + DATABASE_NAME, null, DATABASE_VERSION);

//            SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory()
//                    + File.separator + "ChitFund"
//                    + File.separator + DATABASE_NAME, null);


        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "
                    + TABLE_NAME
                    + " (date TEXT, amount TEXT, ref TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
            Toast.makeText(getApplicationContext(), "i am created", Toast.LENGTH_SHORT).show();
        }

        //drop table
        public void dropTable() {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("CREATE TABLE "
                    + TABLE_NAME
                    + " (date TEXT, amount TEXT, ref TEXT)");
            db.close();
        }

        // Adding new entry
        public void addEntry(Sheet contact) {
            SQLiteDatabase db = this.getWritableDatabase();

            String ROW1 = "INSERT INTO " + TABLE_NAME + " Values ('" + contact.date + "', '" + contact.amount + "', '" + contact.ref + "')";
            db.execSQL(ROW1);
            db.close(); // Closing database connection
        }
    }

    private void exportDB(OpenHelper dbhelper) {

        File exportDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + "ChitFund"
                + File.separator, "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "databse.csv");
//        File file2 = new File(selectedFilePath);
//        boolean deleted = file.delete();
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }
}
