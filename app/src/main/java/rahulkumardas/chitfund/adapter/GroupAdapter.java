package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.RentGroup;

/**
 * Created by Rahul Kumar Das on 26-12-2016.
 */

public class GroupAdapter extends BaseAdapter {

    public GroupAdapter(Context context, List<RentGroup> list) {
        this.context = context;
        this.list = list;
    }

    Context context;
    List<RentGroup> list;
    TextView name, date, rent_count;


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;

        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_rent_group, parent, false);
            name = (TextView) v.findViewById(R.id.name);
            date = (TextView) v.findViewById(R.id.date);
            rent_count = (TextView) v.findViewById(R.id.rent_count);
        } else {
            v = convertView;
        }

        name.setText(list.get(position).name);
        if (list.get(position).date.contains("/")) {
            date.setText("Dated - " + list.get(position).date);
            rent_count.setText("Total Rent Count : " + list.get(position).rent_count + "");
        } else {
            date.setText("Balance : " + list.get(position).rent_count);
            rent_count.setText("Name : " + list.get(position).date);
        }

        return v;
    }
}
