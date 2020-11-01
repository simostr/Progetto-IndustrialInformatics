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
import com.ing.simtr.domoticcontrol.utility.SubscriptionElementOPC;

import java.util.List;

public class SubscriptionAdapter extends ArrayAdapter<SubscriptionElementOPC> {

    public SubscriptionAdapter(Context context, int resource, List<SubscriptionElementOPC> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView=inflater.inflate(R.layout.card_view_subscription, null);

        TextView c_subID=convertView.findViewById(R.id.card_subID);
        TextView c_sessionid=convertView.findViewById(R.id.card_sessID);
        TextView c_pubint=convertView.findViewById(R.id.card_pubInt);
        TextView c_lifetime=convertView.findViewById(R.id.card_lifetime);
        TextView c_keepalive=convertView.findViewById(R.id.card_keepalive);

        SubscriptionElementOPC obj=getItem(position);

        c_subID.setText(obj.getSubscription().getSubscriptionId().toString());
        c_sessionid.setText(obj.getSessionChannel().getSession().getName());
        c_pubint.setText(obj.getSubscription().getRevisedPublishingInterval().toString());
        c_lifetime.setText(obj.getSubscription().getRevisedLifetimeCount().toString());
        c_keepalive.setText(obj.getSubscription().getRevisedMaxKeepAliveCount().toString());

        return convertView;

    }
}
