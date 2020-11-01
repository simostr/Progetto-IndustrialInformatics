package com.ing.simtr.domoticcontrol.utility.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ing.simtr.domoticcontrol.R;
import com.ing.simtr.domoticcontrol.utility.MonitoredItemElementOPC;

import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.core.MonitoredItemNotification;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

public class SubMonitoringAdapter extends ArrayAdapter<MonitoredItemElementOPC> {

    public SubMonitoringAdapter(Context context, int resource, List<MonitoredItemElementOPC> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView= inflater.inflate(R.layout.list_submonitoring,null);
        MonitoredItemElementOPC obj= getItem(position);
        TextView txtMonID= convertView.findViewById(R.id.txtMonID);
        TextView submonval= convertView.findViewById(R.id.txtsubmonval);
        TextView substatus= convertView.findViewById(R.id.txtsubstato);
        TextView subnodeid=convertView.findViewById(R.id.txtNodeID);

        txtMonID.setText("Item ID: "+obj.getMonitoredItem().getResults()[0].getMonitoredItemId());
        subnodeid.setText(obj.getMonitoredItemRequest().getItemsToCreate()[0].getItemToMonitor().getNodeId().toString());
        try {
            MonitoredItemNotification notification = obj.getReadings().getFirst();
            submonval.setText("Val: " + notification.getValue().getValue());
            substatus.setText("Status: " + notification.getValue().getStatusCode());
        }catch (NoSuchElementException e){
            submonval.setText("Val: ");
            substatus.setText("Status: ");
        }
        return convertView;
    }
}
