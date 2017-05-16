package rahulkumardas.chitfund.ui.rent;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import java.util.ArrayList;
import java.util.List;

import rahulkumardas.chitfund.R;

public class RentActivityTwo extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private ViewPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.mipmap.village,
            R.mipmap.shop,
            R.mipmap.date,
            R.mipmap.address
    };
    private String name;
    private int currRot = 0, finalRot = 45, anInt = 0;
    private int rentCount = 0;
    private RotateAnimation rotate, revRotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_two);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = getIntent().getStringExtra("name");
        setTitle(name);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);


        rotate = new RotateAnimation(currRot, finalRot, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        revRotate = new RotateAnimation(finalRot, currRot, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(100);
        revRotate.setDuration(100);
        rotate.setFillEnabled(true);
        revRotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        revRotate.setFillAfter(true);
        rotate.setInterpolator(new LinearInterpolator());
        revRotate.setInterpolator(new LinearInterpolator());
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (anInt == 0) {
                    fab.startAnimation(rotate);
                    anInt = 1;
                } else {
                    fab.startAnimation(revRotate);
                    anInt = 0;
                }

                Intent i = new Intent(RentActivityTwo.this, AddNewRentActivity.class);
                i.putExtra("name", name);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    // setup view pager
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(HousesFragment.newInstance(name), "Houses");
        adapter.addFragment(ShopsFragment.newInstance(name), "Shops");
        adapter.addFragment(FlatsFragment.newInstance(name), "Flats");
        adapter.addFragment(OthersFragment.newInstance(name), "Others");
        viewPager.setAdapter(adapter);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
