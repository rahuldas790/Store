package rahulkumardas.chitfund.ui.bank;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.GroupAdapter;
import rahulkumardas.chitfund.models.RentGroup;
import rahulkumardas.chitfund.ui.ChitFundApplication;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BankFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String USER = ChitFundApplication.USER;
    private static final String BANK = ChitFundApplication.BANK;
    private DatabaseReference ref = ChitFundApplication.reference;

    private String mParam1;
    private String mParam2;
    private Button create, delete;
    private List<RentGroup> banks;
    private ListView list;
    private ProgressBar bar;

    public BankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BankFragment.
     */
    public static BankFragment newInstance(String param1, String param2) {
        BankFragment fragment = new BankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = ref.child(USER + BANK);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bank, container, false);
        create = (Button) view.findViewById(R.id.createButton);
        delete = (Button) view.findViewById(R.id.deleteButton);
        list = (ListView) view.findViewById(R.id.listView);
        bar = (ProgressBar) view.findViewById(R.id.loading);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateBankAccount.class));
            }
        });
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                banks = new ArrayList<RentGroup>();
                Iterator<DataSnapshot> accounts = dataSnapshot.getChildren().iterator();
                while (accounts.hasNext()) {
                    DataSnapshot account = accounts.next();
                    String key = account.getKey();
                    String name = account.child("name").getValue(String.class);
                    float balance = account.child("balance").getValue(Float.class);
                    banks.add(new RentGroup(key, name, (int) balance));
                }

                GroupAdapter adapter = new GroupAdapter(getActivity(), banks);
                list.setAdapter(adapter);
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ViewBankActivity.class);
                i.putExtra("key", banks.get(position).name);
                startActivity(i);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (banks == null) {
                    ChitFundApplication.makeToast("Please wait while accounts are loading", TastyToast.WARNING);
                } else {
                    AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                    ad.setTitle("Tap on the account to delete");
                    ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, banks) {
                        @NonNull
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);

                            TextView text = (TextView) view.findViewById(android.R.id.text1);
                            text.setText(banks.get(position).name);
                            return view;
                        }
                    };
                    ad.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {

                            ref.child(banks.get(which).name).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    dialog.dismiss();
                                }
                            });


                        }
                    });
                    ad.show();
                }


            }
        });

        return view;
    }

}
