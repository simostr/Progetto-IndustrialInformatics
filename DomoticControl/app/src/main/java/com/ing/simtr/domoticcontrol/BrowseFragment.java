package com.ing.simtr.domoticcontrol;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ing.simtr.domoticcontrol.utility.AdapterClass.NodeAdapter;
import com.ing.simtr.domoticcontrol.utility.BrowseDataStamp;

import java.util.ArrayList;

public class BrowseFragment extends Fragment {
    Bundle bundle;
    ListView listNode;
    NodeAdapter adapter;
    ArrayList<String> nodes,namespace,nodeindex,classi;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_browsing, container, false);

        bundle=getArguments();
        listNode =  view.findViewById(R.id.listNode);

        nodes = bundle.getStringArrayList("Nodi");
        namespace= bundle.getStringArrayList("namespace");
        nodeindex= bundle.getStringArrayList("nodeindex");
        classi= bundle.getStringArrayList("nodeclass");

        ArrayList<BrowseDataStamp> dati=new ArrayList<>();
        for(int i = 0; i< nodes.size(); i++){
            BrowseDataStamp tmp= new BrowseDataStamp(nodes.get(i),namespace.get(i),nodeindex.get(i),classi.get(i));
            dati.add(tmp);
        }

        adapter= new NodeAdapter(getContext(),R.layout.card_browsing,dati);
        listNode.setAdapter(adapter);

                listNode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView v=view.findViewById(R.id.card_node_txtName);
                TextView v1=view.findViewById(R.id.card_node_txtnamespace);
                TextView v2=view.findViewById(R.id.card_node_txtnodeindex);
                TextView v3=view.findViewById(R.id.card_node_txtclass);


                ((BrowsingActivity) getActivity()).BrowseDaRadice(position, v.getText().toString(), v1.getText().toString(),
                            v2.getText().toString(), v3.getText().toString());

            }
        });

        return view;
    }

}
