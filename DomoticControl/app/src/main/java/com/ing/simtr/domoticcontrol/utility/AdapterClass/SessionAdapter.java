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
import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;

import java.util.List;

public class SessionAdapter extends ArrayAdapter<SessionElementOPC> {

    public SessionAdapter(Context context, int resource, List<SessionElementOPC> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView=inflater.inflate(R.layout.card_view_session, null);

        TextView txtUrl=convertView.findViewById(R.id.card_session_url);
        TextView txtSessionID=convertView.findViewById(R.id.card_session_sessionID);
        TextView txtEndpoint=convertView.findViewById(R.id.card_session_endpoint);
        TextView txtSecurityMode=convertView.findViewById(R.id.card_session_security_mode);

        SessionElementOPC obj=getItem(position);

        txtUrl.setText(obj.getUrl());
        txtSessionID.setText(obj.getSession().getSession().getName());
        txtEndpoint.setText(obj.getSession().getSession().getEndpoint().getEndpointUrl());
        txtSecurityMode.setText(obj.getSession().getSession().getEndpoint().getSecurityMode().toString());


        return convertView;
    }
}
