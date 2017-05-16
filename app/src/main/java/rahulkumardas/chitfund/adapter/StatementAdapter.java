package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.ChitList;
import rahulkumardas.chitfund.models.Statement;

/**
 * Created by Rahul Kumar Das on 25-10-2016.
 */

public class StatementAdapter extends BaseAdapter {

    final Context context;
    final List<Statement> list;

    public StatementAdapter(Context context, List<Statement> list) {

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
        Statement item = list.get(position);
        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_mini_statement, parent, false);
            holder = new Holder();
            holder.amount = (TextView) v.findViewById(R.id.amount);
            holder.date = (TextView) v.findViewById(R.id.date);
            holder.type = (TextView) v.findViewById(R.id.type);
            holder.description = (TextView) v.findViewById(R.id.description);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (Holder) v.getTag();
        }

        String s1 = context.getResources().getString(R.string.Rs);
        holder.amount.setText("Amount : " + s1 +" "+ String.valueOf(item.amount));
        holder.date.setText(item.date.split(" ")[0]+" "+item.date.split(" ")[1]+" "+item.date.split(" ")[2]+" "+item.date.split(" ")[3]);
        holder.type.setText("Type: "+item.type);

        if (item.type.equals("deposit"))
            holder.description.setText("Reference : " + item.description);
        else if (item.type.equals("withdraw"))
            holder.description.setText("Reason : " + item.description);
        else if (item.type.equals("transfer") && item.mode.equals("online"))
            holder.description.setText("Description : " + item.description);
        else if (item.type.equals("transfer") && item.mode.equals("cheque"))
            holder.description.setText("Cheque : " + item.description);

        return v;
    }

    public class Holder {
        TextView amount, date, type, description;
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
