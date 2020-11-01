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

import org.opcfoundation.ua.core.MonitoredItemNotification;

import java.util.List;

public class BuffReadMonitoringAdapter extends ArrayAdapter<MonitoredItemNotification> {

    public BuffReadMonitoringAdapter(Context context, int resource, List<MonitoredItemNotification> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView= inflater.inflate(R.layout.card_view_buffervalue_mon_i,null);

        TextView mb_val=convertView.findViewById(R.id.card_buff_val);
        TextView mb_set=convertView.findViewById(R.id.card_buff_set);
        TextView mb_sot=convertView.findViewById(R.id.card_buff_sot);
        TextView mb_status=convertView.findViewById(R.id.card_buff_status);

        MonitoredItemNotification obj=getItem(position);

        mb_val.setText(obj.getValue().getValue().toString());
        mb_set.setText(""+obj.getValue().getServerTimestamp());
        mb_sot.setText(""+obj.getValue().getSourceTimestamp());
        mb_status.setText(obj.getValue().getStatusCode().toString());

        System.out.println("Debug: "+obj.getValue().getValue()+"\n"+
                    obj.getValue().getServerTimestamp()+"\n"+
                    obj.getValue().getSourceTimestamp()+"\n"+
                    obj.getValue().getStatusCode());
        return convertView;
    }
}
