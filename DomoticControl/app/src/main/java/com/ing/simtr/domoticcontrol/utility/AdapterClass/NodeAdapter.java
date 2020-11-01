package com.ing.simtr.domoticcontrol.utility.AdapterClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ing.simtr.domoticcontrol.R;
import com.ing.simtr.domoticcontrol.utility.BrowseDataStamp;

import java.util.List;

public class NodeAdapter extends ArrayAdapter<BrowseDataStamp> {

    public NodeAdapter(Context context, int resource, List<BrowseDataStamp> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView= inflater.inflate(R.layout.card_browsing,null);

        TextView node= convertView.findViewById(R.id.card_node_txtName);
        TextView nodeNamespace= convertView.findViewById(R.id.card_node_txtnamespace);
        TextView nodeIndex=convertView.findViewById(R.id.card_node_txtnodeindex);
        TextView nodeClass=convertView.findViewById(R.id.card_node_txtclass);

        BrowseDataStamp obj= getItem(position);

        node.setText(obj.name);
        nodeNamespace.setText(obj.namespace);
        nodeIndex.setText(obj.nodeindex);
        nodeClass.setText(obj.nodeclass);

        return convertView;
    }
}
