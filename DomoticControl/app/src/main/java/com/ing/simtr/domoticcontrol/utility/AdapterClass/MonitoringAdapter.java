package com.ing.simtr.domoticcontrol.utility.AdapterClass;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ing.simtr.domoticcontrol.R;
import com.ing.simtr.domoticcontrol.utility.SubscriptionElementOPC;

import java.util.List;

public class MonitoringAdapter extends ArrayAdapter<SubscriptionElementOPC> {

    SubMonitoringAdapter adapter;

    public MonitoringAdapter(Context context, int resource, List<SubscriptionElementOPC> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView= inflater.inflate(R.layout.list_monitoring,null);
        SubscriptionElementOPC obj= getItem(position);

        TextView txtSubID= convertView.findViewById(R.id.txtSubID);
        ListView listSub= convertView.findViewById(R.id.listSubMonitored);
        TextView txtpubinterval= convertView.findViewById(R.id.txtSubPubInterval);

        ViewGroup.LayoutParams l= listSub.getLayoutParams();
        l.height= (int)getContext().getResources().getDimension(R.dimen.dim)*obj.getMonitoredItemElementOPCS().size();
        listSub.setLayoutParams(l);

        txtSubID.setText("SubscriptionElementOPC ID: "+obj.getSubscription().getSubscriptionId());
        txtpubinterval.setText("Publishing interval: "+obj.getSubscription().getRevisedPublishingInterval().toString()+" ms");
        adapter= new SubMonitoringAdapter(getContext(),R.layout.list_submonitoring,obj.getMonitoredItemElementOPCS());

        listSub.setAdapter(adapter);

        return convertView;
    }
}
