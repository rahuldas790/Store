package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.ChitDetailsMonthly;
import rahulkumardas.chitfund.ui.ChitFundApplication;

/**
 * Created by Rahul Kumar Das on 27-10-2016.
 */

public class MonthlyViewAdapter extends BaseAdapter {

    List<ChitDetailsMonthly> list;
    Context context;

    public MonthlyViewAdapter(List<ChitDetailsMonthly> list, Context context) {
        this.list = list;
        this.context = context;
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

        View v;
        Holder holder;
        if(convertView==null){

            v = LayoutInflater.from(context).inflate(R.layout.item_chit_details_monthly, null, false);
            holder = new Holder();
            holder.amount = (TextView)v.findViewById(R.id.amount);
            holder.name = (TextView)v.findViewById(R.id.month);
            holder.bidAmount = (TextView)v.findViewById(R.id.bidAmt);
            holder.bidType = (TextView)v.findViewById(R.id.bidType);
            v.setTag(holder);

        }else{
            v = convertView;
            holder = (Holder)v.getTag();
        }

        ChitDetailsMonthly item = list.get(position);
        Log.i(ChitFundApplication.TAG, item.amount+" "+item.name+" "+item.bid+" "+item.type);
        holder.amount.setText(item.amount);
        holder.name.setText(item.name);
        holder.bidAmount.setText(item.bid);
        holder.bidType.setText(item.type);

        return v;
    }

    public class Holder{
        TextView name;
        TextView amount;
        TextView bidAmount;
        TextView bidType;
    }
}
