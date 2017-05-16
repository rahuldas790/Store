package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import rahulkumardas.chitfund.R;
import rahulkumardas.chitfund.ui.chit.AddMemberNameActivity;
import rahulkumardas.chitfund.ui.gold.AddMemberNameActivityGold;

/**
 * Created by Rahul Kumar Das on 28-10-2016.
 */

public class AddMemberNameAdapter extends BaseAdapter {

    public AddMemberNameAdapter(List<String> names, Context context, boolean[] checked) {
        super();
        this.names = names;
        this.context = context;
        this.checked = checked;
        if(context.getClass()== AddMemberNameActivity.class){
            count = AddMemberNameActivity.count;
            countTv = AddMemberNameActivity.countTv;
        }else{
            count = AddMemberNameActivityGold.count;
            countTv = AddMemberNameActivityGold.countTv;
        }
    }

    private List<String> names;
    private Context context;
    private Holder holder;
    public boolean[] checked;
    public TextView countTv;
    public int count;

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_add_member_name, null, false);
            holder = new Holder();

            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();
                    checked[getPosition] = buttonView.isChecked();
//                    if (buttonView.isChecked())
//                        count++;
//                    else
//                        count--;

//                    countTv.setText(count + "");
                }
            });
            convertView.setTag(holder);
            convertView.setTag(R.id.name, holder.name);
            convertView.setTag(R.id.checkbox, holder.checkBox);

        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.checkBox.setTag(position);

        holder.name.setText(names.get(position));

        holder.checkBox.setChecked(checked[position]);
        count=0;
        for (int i=0;i<checked.length;i++){
            if(checked[i]){
                count++;
            }
        }
        countTv.setText(count + "");

        return convertView;
    }

    public class Holder {
        TextView name;
        CheckBox checkBox;
    }
}
