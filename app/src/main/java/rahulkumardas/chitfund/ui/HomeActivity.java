package rahulkumardas.chitfund.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.RestAdapterAPI;
import rahulkumardas.chitfund.ui.bank.BankFragment;
import rahulkumardas.chitfund.ui.chit.AddNewMember;
import rahulkumardas.chitfund.ui.chit.ChitFragment;
import rahulkumardas.chitfund.ui.chit.SearchMemberActivity;
import rahulkumardas.chitfund.ui.gold.GoldFragment;
import rahulkumardas.chitfund.ui.gold.SearchMemberActivityGold;
import rahulkumardas.chitfund.ui.rent.RentFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static rahulkumardas.chitfund.ui.ChitFundApplication.EIGHT_DAY_DUR;
import static rahulkumardas.chitfund.ui.ChitFundApplication.SEVEN_DAY_DUR;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private String title = "Home(Normal)";
    private Toolbar toolbar;
    private Fragment fragment;
    private DatabaseReference ref = ChitFundApplication.reference;
    private final String USER = ChitFundApplication.USER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragment = new ChitFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, fragment);
        ft.commit();

        ref = ref.child(USER);
        setTimeStamp();
        Toast.makeText(this, "Reference is "+ref, Toast.LENGTH_SHORT).show();
    }

    private void setTimeStamp() {
        ref.child("sheetStamp/currentStamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                checkForUpdate();
            }
        });
    }

    private void checkForUpdate(){

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

        Call<JsonObject> result = adapterAPI.getCurrentStamp();
        result.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                JsonObject object = response.body();
                long currentStamp = object.get("currentStamp").getAsLong();
                long bSheetStamp = 0;
                if (object.has("bSheetStamp")) {
                    bSheetStamp = object.get("bSheetStamp").getAsLong();
                } else {
                    ref.child("sheetStamp/bSheetStamp").setValue(currentStamp);
                    bSheetStamp = currentStamp;
                }

                long diff = currentStamp - bSheetStamp;
                if (diff >= SEVEN_DAY_DUR && diff < EIGHT_DAY_DUR) {
                    Toast.makeText(HomeActivity.this, "It is the last day to download the balance sheet data", Toast.LENGTH_SHORT).show();
                } else if (diff >= EIGHT_DAY_DUR) {
                    bSheetStamp = bSheetStamp + SEVEN_DAY_DUR;
                    ref.child("sheetStamp/bSheetStamp").setValue(bSheetStamp);
                    ref.child("sheet").removeValue();
                }else{
                        Toast.makeText(HomeActivity.this, "Remained "+(SEVEN_DAY_DUR - diff), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error is "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            if (title.equals(getResources().getString(R.string.title_normal_home))) {
                startActivity(new Intent(this, SearchMemberActivity.class));
            } else if (title.equals(getResources().getString(R.string.title_gold_home))) {
                startActivity(new Intent(this, SearchMemberActivityGold.class));
            } else if (title.equals(getResources().getString(R.string.title_rent_home))) {

            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.chit) {
            // Handle the camera action
            title = getResources().getString(R.string.title_normal_home);
            fragment = new ChitFragment();
        } else if (id == R.id.gold_chit) {
            title = getResources().getString(R.string.title_gold_home);
            fragment = new GoldFragment();
        } else if (id == R.id.rent) {
            title = getResources().getString(R.string.title_rent_home);
            fragment = new RentFragment();
        } else if (id == R.id.bank) {
            title = getResources().getString(R.string.title_blah_home);
            fragment = new BankFragment();
        } else if (id == R.id.member) {
            startActivity(new Intent(this, AddNewMember.class));
        } else if (id == R.id.sheet) {
            startActivity(new Intent(this, BalanceSheetActivity.class));
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout, fragment);
            ft.commit();
        }

        toolbar.setTitle(title);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
