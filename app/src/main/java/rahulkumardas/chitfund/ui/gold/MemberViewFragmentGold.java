package rahulkumardas.chitfund.ui.gold;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.MemberViewAdapter;
import rahulkumardas.chitfund.models.ChitDetailsMember;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.chit.MemberDetailsActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MemberViewFragmentGold#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberViewFragmentGold extends Fragment {

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
    public static List<ChitDetailsMember> list;
    public static MemberViewAdapter adapter;


    public MemberViewFragmentGold() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberViewFragment.
     */
    public static MemberViewFragmentGold newInstance(String param1, String param2, String param3, String param4, String param5) {
        MemberViewFragmentGold fragment = new MemberViewFragmentGold();
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
        View view = inflater.inflate(R.layout.fragment_member_view, container, false);
        ref = ref.child(USER + GOLD + "/Chits/" + chitId + "/members/");
        memberCapacityTxt = (TextView) view.findViewById(R.id.member_capacity);
        memberCapacityTxt.setText(chitCapacity);
        availableTxt = (TextView) view.findViewById(R.id.available_member);
        availableTxt.setText(available);
        chitAmountvTxt = (TextView) view.findViewById(R.id.amount);
        chitAmountvTxt.setText(chitAmount);
        chitDateTxt = (TextView) view.findViewById(R.id.date);
        chitDateTxt.setText(chitDate);
        listView = (ListView) view.findViewById(R.id.recyclerView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), MemberDetailsActivity.class);
                i.putExtra("amount", chitAmount);
                i.putExtra("date", chitDate);
                i.putExtra("chitId", chitId);
                i.putExtra("memberId", position + "");
                startActivity(i);
            }
        });
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
                    long count = dataSnapshot.getChildrenCount();
                    for (int i = 0; i < count; i++) {

                        String pending, bidMonth, bidBy, paid, gift, name;
                        pending = dataSnapshot.child(i + "/pending").getValue(Float.class) + "";
                        bidMonth = dataSnapshot.child(i + "/bidMonth").getValue(String.class);
                        bidBy = dataSnapshot.child(i + "/bidBy").getValue(String.class);
                        paid = dataSnapshot.child(i + "/paid").getValue(Float.class) + "";
                        gift = dataSnapshot.child(i + "/gift").getValue(Float.class) + "";
                        name = dataSnapshot.child(i + "/name").getValue(String.class);

                        Log.i(ChitFundApplication.TAG, pending + " " + bidMonth + " " + bidBy);
                        ChitDetailsMember item = new ChitDetailsMember(name, gift, paid, pending, bidMonth, bidBy);
                        list.add(item);
                    }

                    adapter = new MemberViewAdapter(list, getActivity());
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
