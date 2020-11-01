package com.ing.simtr.domoticcontrol;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ing.simtr.domoticcontrol.utility.AdapterClass.EndpointsAdapter;
import com.ing.simtr.domoticcontrol.utility.DelayedProgressDialog;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;
import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.CreateSessionThread;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.DiscoveryThread;


import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.builtintypes.StatusCode;
import static org.opcfoundation.ua.utils.EndpointUtil.selectByProtocol;
import static org.opcfoundation.ua.utils.EndpointUtil.sortBySecurityLevel;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    EditText indirizzo_server;
    Button discovery_button;
    ListView result_discovery;
    DelayedProgressDialog dialog;

    ManagerOPC managerOPC;
    EndpointDescription[] endpointDescriptions;
    List<EndpointDescription> endpoints_list;
    EndpointsAdapter adapter;
    String server_url;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        discovery_button=findViewById(R.id.discovery_button);
        discovery_button.setOnClickListener(this);
        indirizzo_server=findViewById(R.id.indirizzo_server);
        result_discovery=findViewById(R.id.list_discovery);

        managerOPC=ManagerOPC.CreateManagerOPC(MainActivity.this);

        endpoints_list=new ArrayList<>();
        adapter=new EndpointsAdapter(getApplicationContext(), R.layout.list_endpoints, endpoints_list);
        result_discovery.setAdapter(adapter);
        result_discovery.setOnItemClickListener(this);

        dialog = new DelayedProgressDialog(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for(SessionElementOPC session:managerOPC.getSessionsList()){
            session.getSession().closeAsync();
        }
        managerOPC.getSessionsList().clear();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.discovery_button:
                endpoints_list.clear();

                if(indirizzo_server.getText().toString().toLowerCase().startsWith("opc.tcp://"))
                    server_url=indirizzo_server.getText().toString();
                else
                    server_url="opc.tcp://"+indirizzo_server.getText().toString();

                Client client=managerOPC.getClient();

                dialog.show(getSupportFragmentManager(), "caricamento");

                DiscoveryThread t=new DiscoveryThread(server_url, client);

                Handler handler_discovery=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        dialog.cancel();
                        dialog.dismiss();
                        if (msg.what==-1){
                            Toast.makeText(getApplicationContext(), R.string.EndPointsNonTrovati+
                                    ((StatusCode)msg.obj).getDescription()+"\nCode"+
                                    ((StatusCode)msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                        }else if(msg.what==-2){
                            Toast.makeText(getApplicationContext(), R.string.ServerDown,Toast.LENGTH_LONG).show();
                        }else{
                            endpointDescriptions =selectByProtocol(sortBySecurityLevel(
                                    (EndpointDescription[])msg.obj), "opc.tcp");
                            for(int i=0; i<endpointDescriptions.length;i++){
                                endpoints_list.add(endpointDescriptions[i]);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                };
                t.start(handler_discovery);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if(endpointDescriptions[position].getEndpointUrl().toLowerCase().startsWith("opc.tcp")){
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.SelezioneEndpoint);
            final String message=endpointDescriptions[position].getEndpointUrl()+"\n"+
                    "SecurityMode: "+endpointDescriptions[position].getSecurityMode()+"\n"+
                    "SecurityPolicy: "+endpointDescriptions[position].getSecurityPolicyUri()+"\n"+
                    "SecurityLevel: "+endpointDescriptions[position].getSecurityLevel();
            builder.setMessage(message);
            DialogInterface.OnClickListener listener= new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogIn, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            CreateSessionThread t=new CreateSessionThread(
                                    managerOPC,  endpointDescriptions[position], server_url);
                            dialog.show(getSupportFragmentManager(), getString(R.string.attendi));
                            Handler handler_createssession=new Handler(){
                                @Override
                                public void handleMessage(Message msg) {
                                    dialog.cancel();
                                    if(msg.what==-1){
                                        String m=getString(R.string.SessioneNonCreata)+"\n"+
                                                ((StatusCode)msg.obj).getDescription()+"\n"+
                                                "\nCode: "+((StatusCode)msg.obj).getValue().toString();
                                        System.out.println(m);
                                        Toast.makeText(getApplicationContext(),m,Toast.LENGTH_LONG).show();
                                    }else if(msg.what==-2){
                                        Toast.makeText(getApplicationContext(),R.string.ServerDown,Toast.LENGTH_LONG).show();
                                    } else{
                                        int session_position= (int)msg.obj;
                                        Intent intent= new Intent(MainActivity.this, SessionActivity.class);
                                        intent.putExtra("SessionPosition",session_position);
                                        intent.putExtra("Url", endpointDescriptions[position].getEndpointUrl());
                                        startActivity(intent);
                                    }
                                }
                            };
                            t.start(handler_createssession);
                            dialogIn.dismiss();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialogIn.dismiss();
                            break;
                    }
                }
            };
            builder.setPositiveButton(android.R.string.yes, listener);
            builder.setNegativeButton(android.R.string.no, listener);
            builder.setCancelable(false);
            Dialog g=builder.create();
            g.show();
        }else{
            AlertDialog alertDialog=new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(R.string.NonSupportato);
            alertDialog.setMessage(getString(R.string.ProtocolloNonSupportato));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogI, int which) {
                            dialogI.dismiss();
                        }
                    });
            alertDialog.show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.action_sessions:
                intent=new Intent(MainActivity.this, SelectSessionsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_info:
                AlertDialog alertDialog=new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Information");
                alertDialog.setMessage(getString(R.string.infoMessage));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
