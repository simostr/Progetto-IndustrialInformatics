package com.ing.simtr.domoticcontrol.utility.AdapterClass;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ing.simtr.domoticcontrol.R;
import com.ing.simtr.domoticcontrol.utility.MonitoredItemElementOPC;

import org.opcfoundation.ua.core.MonitoredItemCreateResult;

import java.util.List;

public class MonitoredItemsAdapter extends ArrayAdapter<MonitoredItemElementOPC> {

    public MonitoredItemsAdapter(Context context, int resource, List<MonitoredItemElementOPC> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView=inflater.inflate(R.layout.card_view_monitoreditems, null);

        TextView cm_itemID=convertView.findViewById(R.id.card_m_itemid);
        TextView cm_sampint=convertView.findViewById(R.id.card_m_sampint);
        TextView cm_queue=convertView.findViewById(R.id.card_m_queue);

        MonitoredItemElementOPC obj=getItem(position);

        MonitoredItemCreateResult c=obj.getMonitoredItem().getResults()[0];

        cm_itemID.setText(c.getMonitoredItemId().toString());
        cm_sampint.setText(c.getRevisedSamplingInterval().toString());
        cm_queue.setText(c.getRevisedQueueSize().toString());

        return convertView;
    }
}
