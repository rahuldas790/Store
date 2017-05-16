package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.ReceiveModelRent;
import rahulkumardas.chitfund.ui.rent.ReceiveAmountRent;

/**
 * Created by Rahul Kumar Das on 28-12-2016.
 */

public class ReceiveAmountRentAdapter extends BaseAdapter {

    public ReceiveAmountRentAdapter(Context context, List<ReceiveModelRent> list) {
        this.context = context;
        this.list = list;
    }

    private Context context;
    private List<ReceiveModelRent> list;
    Holder holder;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = null;
        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_receive_rent, parent, false);
            holder = new Holder();
            holder.date = (TextView) v.findViewById(R.id.date);
            holder.amountPaid = (TextView) v.findViewById(R.id.amtPaid);
            holder.amountRemains = (TextView) v.findViewById(R.id.amtRemains);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (Holder) v.getTag();
        }
        holder.amountPaid.setText(list.get(position).paid + "");
        holder.amountRemains.setText(list.get(position).remains + "");
        holder.date.setText(list.get(position).date);

        return v;
    }

    public static class Holder {
        public static TextView date, amountPaid, amountRemains;
    }
}
