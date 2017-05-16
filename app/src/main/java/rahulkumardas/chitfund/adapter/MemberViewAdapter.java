package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.ChitDetailsMember;

/**
 * Created by Rahul Kumar Das on 27-10-2016.
 */

public class MemberViewAdapter extends BaseAdapter {

    public MemberViewAdapter(List<ChitDetailsMember> list, Context context) {
        this.list = list;
        this.context = context;
    }

    private List<ChitDetailsMember> list;
    private Context context;

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
            v = LayoutInflater.from(context).inflate(R.layout.item_chit_details_member, null, false);
            holder = new Holder();
            holder.bidBy = (TextView)v.findViewById(R.id.bidBy);
            holder.bidMonth = (TextView)v.findViewById(R.id.bidMonth);
            holder.name = (TextView)v.findViewById(R.id.name);
            holder.pendingAmount = (TextView)v.findViewById(R.id.pendingAmount);
            v.setTag(holder);
        }else{
            v = convertView;
            holder = (Holder)v.getTag();
        }

        ChitDetailsMember item = list.get(position);
        holder.pendingAmount.setText(item.pending);
        holder.bidMonth.setText(item.bidMonth);
        holder.bidBy.setText(item.bidBy);
        holder.name.setText(item.name);

        return v;
    }

    public class Holder{
        TextView name;
        TextView pendingAmount;
        TextView bidMonth;
        TextView bidBy;
    }
}
