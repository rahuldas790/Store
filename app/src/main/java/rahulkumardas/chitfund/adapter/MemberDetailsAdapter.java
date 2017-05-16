package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.models.MemberDetails;
import rahulkumardas.chitfund.ui.ChitFundApplication;

/**
 * Created by Rahul Kumar Das on 28-11-2016.
 */

public class MemberDetailsAdapter extends BaseAdapter {

    public MemberDetailsAdapter(Context context, List<MemberDetails> list) {
        this.context = context;
        this.list = list;
    }

    Context context;
    List<MemberDetails> list;

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
        View view = convertView;

        if(view==null){
            view = LayoutInflater.from(context).inflate(R.layout.item_member_detail, parent, false);
            holder = new Holder();
            holder.name = (TextView)view.findViewById(R.id.name);
            holder.paid = (TextView)view.findViewById(R.id.paid);
            holder.pending = (TextView)view.findViewById(R.id.pending);
            holder.gift = (TextView)view.findViewById(R.id.gift);
            view.setTag(holder);
        }else{
            holder = (Holder)view.getTag();
        }

        holder.name.setText(list.get(position).name);
        holder.paid.setText(ChitFundApplication.rupeeFormat(list.get(position).paid+""));
        holder.pending.setText(ChitFundApplication.rupeeFormat(list.get(position).pending+""));
        holder.gift.setText(ChitFundApplication.rupeeFormat(list.get(position).gift+""));

        return view;
    }

    public class Holder{
        TextView name, pending, paid, gift;
    }
}
