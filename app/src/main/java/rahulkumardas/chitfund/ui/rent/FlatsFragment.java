package rahulkumardas.chitfund.ui.rent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.RentAdapter;
import rahulkumardas.chitfund.models.RentModel;
import rahulkumardas.chitfund.ui.ChitFundApplication;

import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FlatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FlatsFragment extends Fragment {
    public FlatsFragment() {
        // Required empty public constructor
    }

    public static FlatsFragment newInstance(String name) {

        Bundle args = new Bundle();

        FlatsFragment fragment = new FlatsFragment();
        args.putString("name", name);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView none;
    private ProgressBar progressBar;
    private ListView listView;
    private List<RentModel> list;
    private DatabaseReference ref = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private String RENT = ChitFundApplication.RENT;
    private String name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name = getArguments().getString("name");
        ref = ref.child(USER + RENT + "/Groups/" + name + "/Flat");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_houses, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        none = (TextView) view.findViewById(R.id.none);
        none.setVisibility(View.GONE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ViewRentDetails.class);
                i.putExtra("name", name);
                i.putExtra("type", "Flat");
                i.putExtra("no", list.get(position).no);
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
                    int count = 0;
                    list = new ArrayList<RentModel>();
                    Iterator<DataSnapshot> all = dataSnapshot.getChildren().iterator();
                    while (all.hasNext()) {
                        count++;
                        DataSnapshot one = all.next();
                        String no = one.getKey();
                        String date = one.child("advPaidDate").getValue(String.class);
                        String tenant = one.child("tenantName").getValue(String.class);

                        RentModel model = new RentModel(date, tenant, no);
                        list.add(model);
                    }
//                total.setText(count + "");
                    RentAdapter adapter = new RentAdapter(getActivity(), list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    if (list.size() == 0) {
                        none.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}