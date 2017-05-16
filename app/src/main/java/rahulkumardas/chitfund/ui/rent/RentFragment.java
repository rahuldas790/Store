package rahulkumardas.chitfund.ui.rent;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.adapter.GroupAdapter;
import rahulkumardas.chitfund.models.RentGroup;
import rahulkumardas.chitfund.ui.ChitFundApplication;

import static rahulkumardas.chitfund.ui.ChitFundApplication.dismissProgress;
import static rahulkumardas.chitfund.ui.ChitFundApplication.makeToast;
import static rahulkumardas.chitfund.ui.ChitFundApplication.showProgress;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RentFragment extends Fragment implements AdapterView.OnItemClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private DatabaseReference ref = ChitFundApplication.reference;
    private String USER = ChitFundApplication.USER;
    private String RENT = ChitFundApplication.RENT;

    private String mParam1;
    private String mParam2;
    private Button create, delete;
    private ListView listView;
    private List<RentGroup> list;
    private GroupAdapter adapter;


    public RentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RentFragment.
     */

    public static RentFragment newInstance(String param1, String param2) {
        RentFragment fragment = new RentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rent, container, false);
        ref = ref.child(USER + RENT + "/Groups");
        create = (Button) v.findViewById(R.id.add);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.create_new_group_popup, null);
                final AlertDialog alert = ad.setView(dialogView).create();
                final MaterialEditText rate = (MaterialEditText) dialogView.findViewById(R.id.groupName);
                Button done = (Button) dialogView.findViewById(R.id.done);
                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(rate.getText())) {
                            rate.setError("Field is empty!");
                        } else {
                            addGroup(rate.getText().toString());
                            alert.dismiss();
                        }
                    }
                });
                alert.show();
            }
        });
        delete = (Button) v.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list == null) {
                    ChitFundApplication.makeToast("Please wait while groups are loading", TastyToast.WARNING);
                } else {
                    AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                    ad.setTitle("Tap on the account to delete");
                    ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list) {
                        @NonNull
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);

                            TextView text = (TextView) view.findViewById(android.R.id.text1);
                            text.setText(list.get(position).name);
                            return view;
                        }
                    };
                    ad.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {

                            ref.child(list.get(which).name).removeValue(new DatabaseReference.CompletionListener() {
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
        loadData();
        listView = (ListView) v.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        return v;
    }

    private void loadData() {
        showProgress(getActivity(), "Loading..");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list = new ArrayList();
                Iterator<DataSnapshot> groups = dataSnapshot.getChildren().iterator();
                while (groups.hasNext()) {
                    DataSnapshot group = groups.next();
                    String name = group.child("name").getValue(String.class);
                    Iterator<DataSnapshot> items = group.getChildren().iterator();
                    long count = 0;
                    while (items.hasNext()) {
                        count += items.next().getChildrenCount();
                    }
                    String date = "N/A";

                    try {
                        date = group.child("date").getValue(String.class);
                    } catch (Exception e) {

                    }
                    list.add(new RentGroup(name, date, (int) count));
                }
                adapter = new GroupAdapter(getActivity(), list);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgress();
                makeToast("Something went wrong", TastyToast.ERROR);
            }
        });
    }

    private void addGroup(String name) {
        new AddNewGroup().execute(name, "", "");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = list.get(position).name;
        Intent i = new Intent(getActivity(), RentActivityTwo.class);
        i.putExtra("name", name);
        startActivity(i);
    }

    private class AddNewGroup extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(getActivity());
        }

        @Override
        protected String doInBackground(String[] params) {

            String name = params[0].toString();
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat format = new SimpleDateFormat("dd/mm/yyyy");
            String dateString = format.format(date);
            ref.child(name + "/date").setValue(dateString);
            ref.child(name + "/count").setValue(0);
            ref.child(name + "/name").setValue(name);

            return null;
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            dismissProgress();
            makeToast("Success", TastyToast.SUCCESS);
        }
    }
}
