package rahulkumardas.chitfund.ui.gold;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import rahulkumardas.chitfund.adapter.MonthlyViewAdapter;
import rahulkumardas.chitfund.models.ChitDetailsMonthly;
import rahulkumardas.chitfund.ui.ChitFundApplication;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MonthlyViewFragmentGold#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthlyViewFragmentGold extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";

    private String chitId;
    private String chitAmount;
    private String chitDate;
    private String chitCapacity;
    private String available;
    private TextView memberCapacityTxt, chitAmountvTxt, chitDateTxt;
    public static TextView availableTxt;
    private ListView listView;
    private DatabaseReference ref = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private String GOLD = ChitFundApplication.GOLD;
    public static List<ChitDetailsMonthly> list;
    public static MonthlyViewAdapter adapter;

    public MonthlyViewFragmentGold() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonthlyViewFragment.
     */

    public static MonthlyViewFragmentGold newInstance(String param1, String param2, String param3, String param4, String param5) {
        MonthlyViewFragmentGold fragment = new MonthlyViewFragmentGold();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        args.putString(ARG_PARAM5, param5);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chitId = getArguments().getString(ARG_PARAM1);
            chitAmount = getArguments().getString(ARG_PARAM2);
            chitDate = getArguments().getString(ARG_PARAM3);
            chitCapacity = getArguments().getString(ARG_PARAM4);
            available = getArguments().getString(ARG_PARAM5);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monthly_view, container, false);

        ref = ref.child(USER + GOLD + "/Chits/" + chitId + "/monthly");

        memberCapacityTxt = (TextView) view.findViewById(R.id.member_capacity);
        memberCapacityTxt.setText(chitCapacity);
        availableTxt = (TextView) view.findViewById(R.id.available_member);
        availableTxt.setText(available);
        chitAmountvTxt = (TextView) view.findViewById(R.id.amount);
        chitAmountvTxt.setText(chitAmount);
        chitDateTxt = (TextView) view.findViewById(R.id.date);
        chitDateTxt.setText(chitDate);
        listView = (ListView) view.findViewById(R.id.recyclerView);

        loadData();
        return view;
    }

    private void loadData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.i(ChitFundApplication.TAG, "Full snapshot is " + dataSnapshot);
                    list = new ArrayList();
                    Iterator<DataSnapshot> allMOnths = dataSnapshot.getChildren().iterator();
                    while (allMOnths.hasNext()) {
                        DataSnapshot month = allMOnths.next();
                        String name, amount, bid, type, by, on;
                        name = month.child("name").getValue(String.class);
                        amount = month.child("amount").getValue(String.class);
//                    bid = dataSnapshot.child("/"+i+"/bid").getValue(String.class);
                        bid = "n/a";
//                    if (dataSnapshot.child("default").getValue(Boolean.class))
                        type = "default";
//                    else
//                        type = "user";
                        by = dataSnapshot.child("takenBy").getValue(String.class);
                        on = dataSnapshot.child("takenOn").getValue(String.class);

                        Log.i(ChitFundApplication.TAG, name + " " + amount + " " + bid + " " + type + "" + by + " " + on);
                        ChitDetailsMonthly item = new ChitDetailsMonthly(name, amount, bid, type, by, on);
                        list.add(item);
                    }

                    adapter = new MonthlyViewAdapter(list, getActivity());
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }catch (Exception e){

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
