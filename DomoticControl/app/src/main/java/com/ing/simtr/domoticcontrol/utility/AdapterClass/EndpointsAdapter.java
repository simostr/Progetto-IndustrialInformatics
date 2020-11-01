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

import org.opcfoundation.ua.core.EndpointDescription;

import java.util.List;

public class EndpointsAdapter extends ArrayAdapter<EndpointDescription> {

    public EndpointsAdapter(Context context, int resource, List<EndpointDescription> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        convertView=inflater.inflate(R.layout.list_endpoints, null);
        TextView endpoint=convertView.findViewById(R.id.textEndpoint);
        String text="";
        EndpointDescription obj=getItem(position);
        text += "Uri: "+obj.getEndpointUrl()+"\n";
        text += "Security Mode: "+obj.getSecurityMode()+"\n";
        text += "Security Policy: "+obj.getSecurityPolicyUri()+"\n";
        text += "Security Level: "+obj.getSecurityLevel();
        endpoint.setText(text);
        return convertView;

    }
}
