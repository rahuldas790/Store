package rahulkumardas.chitfund.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.logging.Level;

import dmax.dialog.SpotsDialog;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.RestAdapterAPI;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rahul Kumar Das on 12-10-2016.
 */

public class ChitFundApplication extends Application {

    public static DatabaseReference reference;
    private FirebaseDatabase myRef;
    private static Context context;
    private static AlertDialog pd;
    public static String TAG = "RAHUL";
    public static String USER = "User1";
    public static String GOLD = "/Gold";
    public static String RENT = "/Rent";
    public static String BANK = "/Banks";
    public static final int GOLD_COMM = 6000;
    public static final long ONE_MONTH_DUR = 2629746000L;
    public static final long SEVEN_DAY_DUR = 518400000L;
    public static final long EIGHT_DAY_DUR = 604800000L;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        myRef = FirebaseDatabase.getInstance();
        myRef.setLogLevel(Logger.Level.DEBUG);
        reference = myRef.getReference();
    }

    public static void makeToast(String string, int type) {
        TastyToast.makeText(context, string, TastyToast.LENGTH_SHORT, type);
    }

    public static void showProgress(Context context,String title) {
        pd = new SpotsDialog(context, R.style.Custom);
        pd.setTitle(title);
        pd.show();
    }

    public static void showProgress(Context context) {
        pd = new ProgressDialog(context);
        pd.setMessage("loading...");
        pd.show();
    }

    public static void changeProgressMessage(String msg) {
        if (pd != null) {
            pd.setMessage(msg);
        } else {
            throw new NullPointerException("Change Progress message only when it is running");
        }
    }

    public static void dismissProgress(){
        pd.dismiss();
    }

    public void launchActivity(Class clas){
        Intent i = new Intent(context, clas);
        startActivity(i);
    }

    public static String rupeeFormat(String value){

        Log.i("Rahul", "Rupee is "+value);
        String s1 = context.getResources().getString(R.string.Rs);
        String str = value, dec="";
        String num = str.split("\\.")[0];

        try {
            dec = str.split("\\.")[1];
        }catch (Exception e){
            Log.i(TAG, "Error is "+e.getMessage());
        }
        Log.i("Rahul", "Num is "+num);
        if (num.length() > 3) {
            String nums[] = num.split("");
            int len = num.length(), put = 3,len2=0;
            Log.i("Rahul", "Num length is "+len);
            num = "";
            while (len >= 0) {

                if (len2 == put) {
                    num = nums[len]+"," + num;
                    put += 2;
                } else {
                    num = nums[len] + num;
                }
                len--;
                len2++;
                Log.i("Rahul", "Num len is "+num.length());
                Log.i("Rahul", "len is "+len);
            }
        }

//        return (s1+num + "." + dec);
        return value;
    }

    public static RestAdapterAPI getAdapter(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …

        // add logging as last interceptor
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestAdapterAPI.BASE_END_POINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();


        RestAdapterAPI adapterAPI = retrofit.create(RestAdapterAPI.class);

        return adapterAPI;
    }
}
