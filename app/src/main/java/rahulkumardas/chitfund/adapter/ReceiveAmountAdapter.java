package rahulkumardas.chitfund.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
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
import rahulkumardas.chitfund.models.ReceiveModel;
import rahulkumardas.chitfund.ui.ChitFundApplication;
import rahulkumardas.chitfund.ui.chit.ReceiveAmountActivity;
import rahulkumardas.chitfund.ui.gold.ReceiveAmountActivityGold;

/**
 * Created by Rahul Kumar Das on 12-11-2016.
 */

public class ReceiveAmountAdapter extends BaseAdapter {
    public ReceiveAmountAdapter(List<ReceiveModel> list, Context context) {
        this.list = list;
        this.context = context;
        payMoney = new float[list.size()];
        giftMoney = new float[list.size()];

    }

    private KeyListener keyListener;

    private List<ReceiveModel> list;
    public static float[] payMoney;
    public static float[] giftMoney;
    private Context context;
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

        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_receive_chit, parent, false);
            holder.amount = (TextView) convertView.findViewById(R.id.amount);
            holder.pending = (TextView) convertView.findViewById(R.id.balance);
            holder.pay = (TextView) convertView.findViewById(R.id.payAmount);
            holder.gift = (TextView) convertView.findViewById(R.id.payGift);
            holder.full = (RadioButton) convertView.findViewById(R.id.full);
            holder.partial = (RadioButton) convertView.findViewById(R.id.partial);
            holder.payLayout = (LinearLayout) convertView.findViewById(R.id.payLayout);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.full.setTag(position);
        holder.partial.setTag(position);
        keyListener = holder.pay.getKeyListener();
        holder.amount.setText(ChitFundApplication.rupeeFormat(list.get(position).chitAmount));
        holder.pending.setText(ChitFundApplication.rupeeFormat(list.get(position).pendingAmount + ""));
        holder.full.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isChecked()) {
                    list.get(position).fullSelected = true;
                    list.get(position).partiallySelected = false;
                    list.get(position).giftPay = 0;
                    list.get(position).pay = list.get(position).pendingAmount;
                    notifyDataSetChanged();
                    int pos = (int) buttonView.getTag();
                    payMoney[position] = list.get(position).pendingAmount;
                } else {
                    list.get(position).fullSelected = false;
                    list.get(position).partiallySelected = true;
                    list.get(position).giftPay = 0;
//                    list.get(position).pay = list.get(position).pendingAmount;
                    list.get(position).pay = 0.0f;
//                    payMoney[position] = list.get(position).pendingAmount;
                    payMoney[position] = 0.0f;
                }
                if(context.getClass() == ReceiveAmountActivity.class) {
                    ReceiveAmountActivity.addValue(position, buttonView.isChecked());
                }else{
                    ReceiveAmountActivityGold.addValue(position, buttonView.isChecked());
                }
            }
        });

//        holder.partial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(context.getClass() == ReceiveAmountActivity.class) {
//                    ReceiveAmountActivity.addValue(position, false);
//                }else{
//                    ReceiveAmountActivityGold.addValue(position, false);
//                }
//            }
//        });

        holder.pay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count >= 1) {
                    payMoney[position] = Float.parseFloat(s.toString());
                    list.get(position).pay = Float.parseFloat(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.gift.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count >= 1) {
                    giftMoney[position] = Float.parseFloat(s.toString());
                    list.get(position).giftPay = Float.parseFloat(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return convertView;
    }

    public static class Holder {
        public static TextView amount, pending, pay, gift;
        RadioButton full, partial;
        LinearLayout payLayout;
    }

//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }
//
//    @Override
//    public int getViewTypeCount() {
//        return list.size();
//    }
}
