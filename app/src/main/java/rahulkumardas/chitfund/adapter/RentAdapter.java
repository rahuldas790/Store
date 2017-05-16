package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.RentModel;
import rahulkumardas.chitfund.models.Statement;

/**
 * Created by Rahul Kumar Das on 25-10-2016.
 */

public class RentAdapter extends BaseAdapter {

    final Context context;
    final List<RentModel> list;

    public RentAdapter(Context context, List<RentModel> list) {

        this.context = context;
        this.list = list;
    }

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

        Holder holder;
        View v;
        RentModel item = list.get(position);
        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_mini_statement, parent, false);
            holder = new Holder();
            holder.date = (TextView) v.findViewById(R.id.amount);
            holder.No = (TextView) v.findViewById(R.id.date);
            holder.Date = (TextView) v.findViewById(R.id.type);
            holder.tenant = (TextView) v.findViewById(R.id.description);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (Holder) v.getTag();
        }

        String s1 = context.getResources().getString(R.string.Rs);
        holder.No.setText("No. : " + item.no);
        holder.date.setText(item.date);
        holder.Date.setText("Date: ");
        holder.tenant.setText("Tenant: " + item.tenant);
        return v;
    }

    public class Holder {
        TextView date, No, Date, tenant;
    }

    public String rupeeFormat(String value) {
        value = value.replace(",", "");
        char lastDigit = value.charAt(value.length() - 1);
        String result = "";
        int len = value.length() - 1;
        int nDigits = 0;

        for (int i = len - 1; i >= 0; i--) {
            result = value.charAt(i) + result;
            nDigits++;
            if (((nDigits % 2) == 0) && (i > 0)) {
                result = "," + result;
            }
        }
        return (result + lastDigit);
    }
}
