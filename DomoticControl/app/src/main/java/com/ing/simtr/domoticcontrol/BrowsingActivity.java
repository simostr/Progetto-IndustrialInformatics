package com.ing.simtr.domoticcontrol;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.simtr.domoticcontrol.utility.BackListener;
import com.ing.simtr.domoticcontrol.utility.DelayedProgressDialog;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;
import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.CallMethodThread;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.ReadThread;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.ThreadBrowse;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.WriteThread;

import org.opcfoundation.ua.builtintypes.BuiltinsMap;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Argument;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.CallResponse;
import org.opcfoundation.ua.core.DataTypeDefinition;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.WriteResponse;
import org.opcfoundation.ua.utils.MultiDimensionArrayUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

public class BrowsingActivity extends AppCompatActivity {

    int session_position;
    FragmentManager fragmentManager;
    DelayedProgressDialog dialog;

    int methodNamespace=-1;
    int methodNodeID=-1;
    int hasMethodObjectNamespace=-1;
    int hasMethodObjectNodeID=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browsing);
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new DelayedProgressDialog(BrowsingActivity.this);

        session_position=getIntent().getIntExtra("SessionPosition", -1);
        if(session_position<0){
            Toast.makeText(BrowsingActivity.this, R.string.ERROR ,Toast.LENGTH_LONG).show();
            finish();
        }

        ManagerOPC.getInstance().initStack();

        ArrayList<String> list_basicnodes_name=new ArrayList<>();
        ArrayList<String> list_basicnodes_namespace=new ArrayList<>();
        ArrayList<String> list_basicnodes_nodeindex=new ArrayList<>();
        ArrayList<String> list_basicnodes_class=new ArrayList<>();

        list_basicnodes_name.add("Root");
        list_basicnodes_namespace.add(Identifiers.RootFolder.getNamespaceIndex()+"");
        list_basicnodes_nodeindex.add(Identifiers.RootFolder.getValue().toString());
        list_basicnodes_class.add("Object");

        Bundle nodi= new Bundle();
        nodi.putStringArrayList("Nodi",list_basicnodes_name);
        nodi.putStringArrayList("namespace", list_basicnodes_namespace);
        nodi.putStringArrayList("nodeindex", list_basicnodes_nodeindex);
        nodi.putStringArrayList("nodeclass", list_basicnodes_class);

        BrowseFragment fragmentbase= new BrowseFragment();
        fragmentbase.setArguments(nodi);

        fragmentManager = getSupportFragmentManager();

        fragmentManager.addOnBackStackChangedListener(new BackListener(fragmentManager));

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, fragmentbase);
        fragmentTransaction.commit();
    }

    private <K, V> Stream<K> keys(Map<K, V> map, V value) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey);
    }

    private class btnCallListener implements View.OnClickListener {
        ArrayList<EditText> arrayList;
        Dialog dialog3;
        SessionElementOPC s;

        public btnCallListener(ArrayList<EditText> arrayList, Dialog dialog3, SessionElementOPC s) {
            this.arrayList = arrayList;
            this.dialog3 = dialog3;
            this.s = s;
        }

        @Override
        public void onClick(View v) {
            ArrayList<String> input=new ArrayList<>();
            for(EditText edt:arrayList){
                input.add(edt.getText().toString());
            }
            CallMethodThread t3=new CallMethodThread(s.getSession(), input, methodNamespace,methodNodeID, hasMethodObjectNamespace, hasMethodObjectNodeID);
            Handler handler3=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    dialog.cancel();
                    if (msg.what == -1) {
                        Toast.makeText(getApplicationContext(), getString(R.string.ERROR)+
                                ((StatusCode)msg.obj).getDescription()+"\nCode: "+((StatusCode)msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                    } else if (msg.what == -2) {
                        Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                    } else {
                        dialog3.dismiss();
                        CallResponse res= (CallResponse) msg.obj;
                        System.out.println(res);
                        Toast.makeText(getApplicationContext(), getString(android.R.string.ok)+" "+res.getResponseHeader().getServiceResult(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            };
            t3.start(handler3);
        }
    }

    public void BrowseDaRadice(int position, final String n_name, final String s_ns, final String s_ni, final String n_c){
        ThreadBrowse t= new ThreadBrowse(session_position,position);
        dialog.show(getSupportFragmentManager(), getString(R.string.attendi));
        Handler handler_browse= new Handler(){
            @Override
            public void handleMessage(Message msg) {
                dialog.cancel();
                if(msg.what==-1){
                    Toast.makeText(getApplicationContext(),getString(R.string.ERROR)+((StatusCode)msg.obj).getDescription()+"\nCode: "+((StatusCode)msg.obj).getValue().toString(),Toast.LENGTH_LONG).show();
                }else if(msg.what==-2){
                    Toast.makeText(getApplicationContext(),R.string.ServerDown,Toast.LENGTH_LONG).show();
                } else{
                    boolean hasMethodChild=false;
                    BrowseResponse res= (BrowseResponse)msg.obj;
                    ArrayList<String> tmp_name=new ArrayList<>();
                    ArrayList<String> tmp_namespace=new ArrayList<>();
                    ArrayList<String> tmp_nodeindex=new ArrayList<>();
                    ArrayList<String> tmp_class=new ArrayList<>();
                    for(int i=0;i<res.getResults().length;i++){
                        if (res.getResults()[i].getReferences() != null) {
                            for (int j = 0; j < res.getResults()[i].getReferences().length; j++) {
                                ReferenceDescription ref = res.getResults()[i].getReferences()[j];
                                tmp_name.add(ref.getDisplayName().getText());
                                tmp_namespace.add(ref.getNodeId().getNamespaceIndex() + "");
                                tmp_nodeindex.add(ref.getNodeId().getValue().toString());
                                tmp_class.add(ref.getNodeClass().toString());
                                if(ref.getNodeClass().toString()=="Method"){
                                    hasMethodChild=true;
                                }
                            }
                        }
                    }
                    if(hasMethodChild){
                        hasMethodObjectNamespace=Integer.parseInt(s_ns);
                        hasMethodObjectNodeID=Integer.parseInt(s_ni);
                        System.out.println("Debug: Has Method: namespace-"+hasMethodObjectNamespace+", nodeID-"+hasMethodObjectNodeID);
                    }
                    if(n_c=="Method"){
                        methodNamespace=Integer.parseInt(s_ns);
                        methodNodeID=Integer.parseInt(s_ni);
                    }

                    if(tmp_name.size()>0) {
                        Bundle nodi= new Bundle();
                        nodi.putStringArrayList("Nodi", tmp_name);
                        nodi.putStringArrayList("namespace", tmp_namespace);
                        nodi.putStringArrayList("nodeindex", tmp_nodeindex);
                        nodi.putStringArrayList("nodeclass", tmp_class);

                        BrowseFragment fragmentbase = new BrowseFragment();
                        fragmentbase.setArguments(nodi);

                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, fragmentbase);
                        fragmentTransaction.addToBackStack("fragment");
                        fragmentTransaction.commit();
                    }else{
                        final SessionElementOPC s=ManagerOPC.getInstance().getSessionsList().get(session_position);
                        switch (n_c){
                            case "Variable":
                                if(n_name.startsWith("Input")) {
                                    if (hasMethodObjectNamespace < 0 || hasMethodObjectNodeID < 0 || methodNamespace < 0 || methodNodeID < 0) {
                                        Toast.makeText(BrowsingActivity.this, R.string.ERROR, Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    final Dialog dialog3 = new Dialog(BrowsingActivity.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                                    dialog3.setContentView(R.layout.dialog_call_method);

                                    final LinearLayout layout = dialog3.findViewById(R.id.dialog_call_layout);
                                    final Button bntCall = new Button(getApplicationContext());
                                    bntCall.setText(R.string.dialog_call);

                                    ReadThread t;
                                    t = new ReadThread(s.getSession(), ManagerOPC.DEFAULT_MAXAGE, TimestampsToReturn.Source,
                                            Integer.parseInt(s_ns), Integer.parseInt(s_ni), Attributes.Value);

                                    final ArrayList<EditText> arrayList = new ArrayList<>();
                                    final Handler h = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            if (msg.what == -1) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.ERROR) +
                                                        ((StatusCode) msg.obj).getDescription() + "\nCode: " +
                                                        ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                            } else if (msg.what == -2) {
                                                Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                            } else {
                                                ReadResponse res = (ReadResponse) msg.obj;
                                                DataValue result = res.getResults()[0];
                                                String string_result = result.getValue().toString();
                                                string_result = string_result.substring(1, string_result.length() - 1);
                                                String[] r = string_result.split("Argument: Argument\n");
                                                ArrayList<String[]> list = new ArrayList<>();
                                                for (int i = 1; i < r.length; i++) {
                                                    list.add(r[i].split("\n"));
                                                }

                                                for (int i = 0; i < list.size(); i++) {
                                                    String[] x = list.get(i);
                                                    TextView textView = new TextView(getApplicationContext());
                                                    EditText editText1 = new EditText(getApplicationContext());
                                                    Stream<Class<?>> streamKey = keys(BuiltinsMap.ID_MAP, Integer.parseInt(x[1].split("=")[2]));
                                                    String type = streamKey.findFirst().get().getCanonicalName().replace("java.lang.", "");
                                                    textView.setText("Descrizione: " + x[2].split("=")[1] + "\n"
                                                            + "Tipo: " + type);
                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                    params.setMargins(5, 10, 5, 5);
                                                    textView.setLayoutParams(params);
                                                    layout.addView(textView);
                                                    layout.addView(editText1);
                                                    arrayList.add(editText1);
                                                }
                                                bntCall.setOnClickListener(new btnCallListener(arrayList, dialog3, s));
                                                layout.addView(bntCall);
                                            }
                                        }
                                    };
                                    t.start(h);
                                    dialog3.show();
                                }else if(n_name.startsWith("Output")){
                                    Toast.makeText(getApplicationContext(), "not supported",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    ReadThread t;
                                    t=new ReadThread(s.getSession(), ManagerOPC.DEFAULT_MAXAGE, TimestampsToReturn.Source,
                                            Integer.parseInt(s_ns), Integer.parseInt(s_ni), Attributes.Value);
                                    dialog.show(getSupportFragmentManager(), "read");
                                    final Handler handler=new Handler(){
                                        @Override
                                        public void handleMessage(Message msg) {
                                            if (msg.what == -1) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.ERROR)+
                                                        ((StatusCode)msg.obj).getDescription()+"\nCode: "+((StatusCode)msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                            } else if (msg.what == -2) {
                                                Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                            } else {
                                                dialog.cancel();
                                                final Dialog dialog2= new Dialog(BrowsingActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                                                dialog2.setContentView(R.layout.dialog_read_write);

                                                final TextView readValue=dialog2.findViewById(R.id.dialog_read_write_readvalue);
                                                final EditText writeValue=dialog2.findViewById(R.id.dialog_read_write_writevalue);
                                                final Button btnWrite=dialog2.findViewById(R.id.dialog_read_write_write);

                                                ReadResponse res = (ReadResponse) msg.obj;
                                                readValue.setText(res.getResults()[0].getValue().toString());

                                                btnWrite.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if(writeValue.getText().toString().length()>0){
                                                            System.out.println(writeValue.getText());
                                                            WriteThread wt;
                                                            wt=new WriteThread(s.getSession(), Integer.parseInt(s_ns), Integer.parseInt(s_ni), Attributes.Value, new Variant(writeValue.getText().toString()));
                                                            dialog.show(getSupportFragmentManager(), ".....");
                                                            Handler handler2=new Handler(){
                                                                @Override
                                                                public void handleMessage(Message msg) {
                                                                    dialog.cancel();
                                                                    if (msg.what == -1) {
                                                                        Toast.makeText(getApplicationContext(), getString(R.string.ERROR)+
                                                                                ((StatusCode)msg.obj).getDescription()+"\nCode: "+((StatusCode)msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                                                    } else if (msg.what == -2) {
                                                                        Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                                                    } else {
                                                                        WriteResponse res= (WriteResponse)msg.obj;
                                                                        String response=res.getResults()[0].getDescription();
                                                                        if(response.length()>0)
                                                                            response="\n"+res.getResults()[0].getDescription();
                                                                        Toast.makeText(getApplicationContext(), getString(android.R.string.ok)+response,
                                                                                Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            };
                                                            wt.start(handler2);
                                                            dialog2.dismiss();
                                                        }
                                                    }
                                                });

                                                dialog2.show();
                                            }
                                        }
                                    };
                                    t.start(handler);
                                }
                            break;
                            default:
                                Toast.makeText(BrowsingActivity.this, R.string.noNodi,Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
            }
        };
        t.start(handler_browse);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent b=new Intent(BrowsingActivity.this, SessionActivity.class);
                b.putExtra("SessionPosition", session_position);
                finish();
                startActivity(b);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!ManagerOPC.getInstance().getSessionsList().get(session_position).getSession().getSecureChannel().isOpen()){
            Intent i=new Intent(BrowsingActivity.this, MainActivity.class);
            startActivity(i);
        }
    }
}
