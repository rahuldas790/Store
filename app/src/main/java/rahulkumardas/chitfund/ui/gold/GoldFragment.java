package rahulkumardas.chitfund.ui.gold;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.sdsmdg.tastytoast.TastyToast;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.ChitFundApplication;

/**
 * Use the {@link GoldFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GoldFragment extends Fragment implements View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    private TextView submit, addNewChit, newMember, receive, selectMonth, selectYear;
    private EditText other;

    public GoldFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GoldFragment.
     */

    public static GoldFragment newInstance(String param1, String param2) {
        GoldFragment fragment = new GoldFragment();
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
        View view = inflater.inflate(R.layout.fragment_gold, container, false);

        submit = (TextView) view.findViewById(R.id.submit);
        submit.setOnClickListener(this);
        addNewChit = (TextView) view.findViewById(R.id.add_new);
        addNewChit.setOnClickListener(this);
        newMember = (TextView) view.findViewById(R.id.add_member);
        newMember.setOnClickListener(this);
        receive = (TextView) view.findViewById(R.id.receive);
        receive.setOnClickListener(this);
        selectMonth = (TextView) view.findViewById(R.id.month);
        selectMonth.setOnClickListener(this);
        selectYear = (TextView)view.findViewById(R.id.years);
        selectYear.setOnClickListener(this);
        other = (EditText)view.findViewById(R.id.other);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent i;
        Class listClass = null, receiveClass = null, addMemberClass = null, adddChitClass = null;

        listClass = ChitListActivityGold.class;
        receiveClass = ReceiveAmountActivityGold.class;
        addMemberClass = AddNewMemberGold.class;
        adddChitClass = AddNewChitActivityGold.class;

        switch (id) {
            case R.id.submit:
                if (!selectMonth.getText().toString().equalsIgnoreCase("month")) {
//                    submit.setImageResource(R.mipmap.submit_pressed);
                    i = new Intent(getActivity(), listClass);
                    i.putExtra("month", selectMonth.getText().toString());
                    startActivity(i);
                } else {
                    ChitFundApplication.makeToast("Please select a month", TastyToast.WARNING);
                }
                break;
            case R.id.add_new:
//                addNewChit.setImageResource(R.mipmap.add_pressed);
                i = new Intent(getActivity(), adddChitClass);
                startActivity(i);
                break;
            case R.id.add_member:
//                newMember.setImageResource(R.mipmap.add_member_pressed);
                i = new Intent(getActivity(), addMemberClass);
                startActivity(i);
                break;
            case R.id.receive:
//                receive.setImageResource(R.mipmap.receive_pressed);
                i = new Intent(getActivity(), receiveClass);
                startActivity(i);
                break;
            case R.id.month:
                popupMonth();
                break;
            case R.id.years:
                popupYear();
                break;
        }
    }

    private void popupMonth() {

        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(getActivity(), selectMonth);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.month_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                selectMonth.setText(item.getTitle());
                return true;
            }
        });

        popup.show(); //showing popup menu

    }

    private void popupYear() {

        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(getActivity(), selectYear);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.year_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.three) {
                    selectYear.setText(item.getTitle());
                    selectYear.setVisibility(View.GONE);
                    other.setVisibility(View.VISIBLE);
                }else{
                    selectYear.setText(item.getTitle());
                    other.setText(item.getTitle());
                }
                return true;
            }
        });

        popup.show(); //showing popup menu

    }
}
