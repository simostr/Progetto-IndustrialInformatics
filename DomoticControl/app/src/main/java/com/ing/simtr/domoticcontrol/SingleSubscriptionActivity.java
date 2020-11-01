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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.simtr.domoticcontrol.utility.AdapterClass.MonitoredItemsAdapter;
import com.ing.simtr.domoticcontrol.utility.AdapterClass.SubscriptionAdapter;
import com.ing.simtr.domoticcontrol.utility.DelayedProgressDialog;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;
import com.ing.simtr.domoticcontrol.utility.SubscriptionElementOPC;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.CreateMonitoredItemThread;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.DeleteSubscriptionThread;

import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.DataChangeTrigger;
import org.opcfoundation.ua.core.DeadbandType;
import org.opcfoundation.ua.core.MonitoredItemCreateRequest;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.MonitoringParameters;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;

public class SingleSubscriptionActivity extends AppCompatActivity implements View.OnClickListener {

    static int idchandle=0;
    TextView txtSessionId;
    TextView txtSubscriptionId;
    ListView listMonitoredItem;
    DelayedProgressDialog dialog;
    Button btnNewMonitored;

    MonitoredItemsAdapter adapter;
    ManagerOPC managerOPC;
    SubscriptionElementOPC subscriptionElementOPC;
    int session_position;
    int sub_position;

    //mon_item
    int namespace;
    int nodeId;
    String nodeIdString;
    double samplingInterval;
    UnsignedInteger queueSize;
    boolean discardOldest;
    double deadband;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_subscription);
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new DelayedProgressDialog(SingleSubscriptionActivity.this);

        managerOPC=ManagerOPC.getInstance();
        session_position=getIntent().getIntExtra("SessionPosition",-1);
        sub_position=getIntent().getIntExtra("subPosition", -1);
        if(session_position<0 || sub_position<0){
            Toast.makeText(SingleSubscriptionActivity.this, R.string.ERROR,Toast.LENGTH_LONG).show();
            finish();
        }

        subscriptionElementOPC=managerOPC.getSessionsList().get(session_position).getSubscriptionElementOPCS().get(sub_position);

        txtSessionId=findViewById(R.id.monitoreitem_sessionid_text);
        txtSessionId.setText(managerOPC.getSessionsList().get(session_position).getSession().getSession().getName());
        txtSubscriptionId=findViewById(R.id.monitoreditem_subID_text);
        txtSubscriptionId.setText(subscriptionElementOPC.getSubscription().getSubscriptionId().toString());

        listMonitoredItem=findViewById(R.id.list_monitoreditem);
        adapter=new MonitoredItemsAdapter(SingleSubscriptionActivity.this, R.layout.card_view_monitoreditems,
                subscriptionElementOPC.getMonitoredItemElementOPCS());
        listMonitoredItem.setAdapter(adapter);

        listMonitoredItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(SingleSubscriptionActivity.this, MonitoringItemActivity.class);
                intent.putExtra("SessionPosition", session_position);
                intent.putExtra("subPosition", sub_position);
                intent.putExtra("monPosition", position);
                startActivity(intent);
            }
        });

        btnNewMonitored=findViewById(R.id.button_createmonitored);
        btnNewMonitored.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final MonitoredItemCreateRequest[] monitoredItemReq=new MonitoredItemCreateRequest[1];
        monitoredItemReq[0]=new MonitoredItemCreateRequest();

        final Dialog dialogV = new Dialog(SingleSubscriptionActivity.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        dialogV.setContentView(R.layout.dialog_createmonitored);
        final Spinner timestamps = dialogV.findViewById(R.id.dialog_mi_spinnerTimestamp);
        ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(SingleSubscriptionActivity.this, R.array.timestamps, android.R.layout.simple_spinner_dropdown_item);
        timestamps.setAdapter(spinneradapter);

        final EditText editTextNamespace=dialogV.findViewById(R.id.dialog_mi_edtNamespace);
        final EditText editTextNodeId=dialogV.findViewById(R.id.dialog_mi_edtNodeID);
        final EditText editTextSampling=dialogV.findViewById(R.id.dialog_mi_edtSamplingInterval);
        final EditText editTextQueue=dialogV.findViewById(R.id.dialog_mi_edtQueueSize);
        final EditText editTextDeadband=dialogV.findViewById(R.id.dialog_mi_edtValDeadband);
        final CheckBox checkBoxDiscard=dialogV.findViewById(R.id.dialog_mi_checkDiscardOldest);
        final RadioGroup radioGroupFilter=dialogV.findViewById(R.id.dialog_mi_rdgroupDeadband);

        editTextSampling.setHint("Es: "+ManagerOPC.DEFAULT_SAMPLING_INTERVAL);
        editTextSampling.setText(ManagerOPC.DEFAULT_SAMPLING_INTERVAL.toString());
        editTextQueue.setHint("Es: "+ManagerOPC.DEFAULT_QUEUE_SIZE);
        editTextQueue.setText(ManagerOPC.DEFAULT_QUEUE_SIZE.toString());
        editTextDeadband.setHint("Es: "+ManagerOPC.DEFAULT_ABSOLUTE_DEAD_BAND);
        editTextDeadband.setText(ManagerOPC.DEFAULT_ABSOLUTE_DEAD_BAND.toString());

        Button buttonOkMon=dialogV.findViewById(R.id.dialog_mi_btnOk);

        buttonOkMon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimestampsToReturn timestampsToReturn=null;
                switch (timestamps.getSelectedItem().toString()){
                    case "Server":
                        timestampsToReturn=TimestampsToReturn.Server;
                        break;
                    case "Source":
                        timestampsToReturn=TimestampsToReturn.Source;
                        break;
                    case "Both":
                        timestampsToReturn=TimestampsToReturn.Both;
                        break;
                    case "Neither":
                        timestampsToReturn=TimestampsToReturn.Neither;
                        break;
                }

                String txtNamespace=editTextNamespace.getText().toString();
                String txtNodeId=editTextNodeId.getText().toString();
                String txtSampling=editTextSampling.getText().toString();
                String txtQueue=editTextQueue.getText().toString();
                String txtDeadbandVal=editTextDeadband.getText().toString();

                if(txtNamespace.length()!=0 && txtNodeId.length()!=0 && txtSampling.length()!=0
                    && txtQueue.length()!=0 && txtDeadbandVal.length()!=0){
                    namespace=Integer.parseInt(txtNamespace);
                    try{
                        nodeId=Integer.parseInt(txtNodeId);
                        nodeIdString=null;
                    }catch(Exception e){
                        nodeIdString=txtNodeId;
                    }
                    samplingInterval=Double.parseDouble(txtSampling);
                    queueSize=new UnsignedInteger(txtQueue);
                    discardOldest=checkBoxDiscard.isChecked();
                    DeadbandType deadbandType=null;
                    switch (radioGroupFilter.getCheckedRadioButtonId()){
                        case R.id.dialog_mi_rdAbsolute:
                            deadbandType=DeadbandType.Absolute;
                            break;
                        case R.id.dialog_mi_rdPercentage:
                            deadbandType=DeadbandType.Percent;
                            break;
                    }
                    deadband=Double.parseDouble(txtDeadbandVal);

                    DataChangeFilter dataChangeFilter=new DataChangeFilter();
                    dataChangeFilter.setTrigger(DataChangeTrigger.StatusValueTimestamp);
                    dataChangeFilter.setDeadbandType(new UnsignedInteger(deadbandType.getValue()));
                    dataChangeFilter.setDeadbandValue(deadband);
                    ExtensionObject fil=new ExtensionObject(dataChangeFilter);

                    MonitoringParameters reqParams=new MonitoringParameters();
                    reqParams.setClientHandle(new UnsignedInteger(idchandle++));
                    reqParams.setSamplingInterval(samplingInterval);
                    reqParams.setQueueSize(queueSize);
                    reqParams.setDiscardOldest(discardOldest);
                    reqParams.setFilter(fil);

                    monitoredItemReq[0].setRequestedParameters(reqParams);
                    monitoredItemReq[0].setMonitoringMode(MonitoringMode.Reporting);
                    NodeId nodeId_req;
                    if (nodeIdString==null)
                        nodeId_req=new NodeId(namespace, nodeId);
                    else
                        nodeId_req=new NodeId(namespace, nodeIdString);
                    monitoredItemReq[0].setItemToMonitor(new ReadValueId(nodeId_req, Attributes.Value,
                            null, null));

                    final CreateMonitoredItemsRequest mi=new CreateMonitoredItemsRequest();
                    mi.setSubscriptionId(subscriptionElementOPC.getSubscription().getSubscriptionId());
                    mi.setTimestampsToReturn(timestampsToReturn);
                    mi.setItemsToCreate(monitoredItemReq);

                    CreateMonitoredItemThread t=new CreateMonitoredItemThread(subscriptionElementOPC,mi);
                    dialog.show(getSupportFragmentManager(), getString(R.string.attendi));
                    Handler handler_monitoreditem = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            dialog.cancel();
                            if (msg.what == -1) {
                                Toast.makeText(getApplicationContext(), getString(R.string.ERROR) +
                                        ((StatusCode) msg.obj).getDescription() + "\nCode: " +
                                        ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                            } else if (msg.what == -2) {
                                Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                            } else if (msg.what == -3) {
                                Toast.makeText(getApplicationContext(), getString(R.string.ERROR) + msg.obj.toString(), Toast.LENGTH_LONG).show();
                            } else {
                                adapter.notifyDataSetChanged();
                                listMonitoredItem.setSelection(adapter.getCount() - 1);
                            }
                        }
                    };
                    t.start(handler_monitoreditem);
                    dialogV.dismiss();
                }else{
                    Toast.makeText(SingleSubscriptionActivity.this, R.string.dialog_sub_enterValidValue, Toast.LENGTH_LONG).show();
                }
            }
        });
        dialogV.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.monitemsactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_terminate:
                AlertDialog.Builder builder=new AlertDialog.Builder(SingleSubscriptionActivity.this);
                builder.setTitle(R.string.elimina_subscription);
                builder.setMessage(R.string.elimina_subscription_message);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                DeleteSubscriptionThread t = new DeleteSubscriptionThread(subscriptionElementOPC.getSessionChannel(),
                                        subscriptionElementOPC.getSubscription().getSubscriptionId());
                                dialog.show(getSupportFragmentManager(), getString(R.string.elimina_subscription));
                                Handler handler_delete_subscription = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        dialog.dismiss();
                                        if (msg.what == -1) {
                                            Toast.makeText(SingleSubscriptionActivity.this, getString(R.string.elimina_subscription_errore)
                                                    + ((StatusCode) msg.obj).getDescription() + "\nCode: " +
                                                    ((StatusCode) msg.obj).getValue().toString(), Toast.LENGTH_LONG).show();
                                        } else if (msg.what == -2) {
                                            Toast.makeText(getApplicationContext(), R.string.ServerDown, Toast.LENGTH_LONG).show();
                                        } else {
                                            managerOPC.getSessionsList().get(session_position).getSubscriptionElementOPCS().remove(sub_position);
                                            finish();
                                        }
                                    }
                                };
                                t.start(handler_delete_subscription);
                                dialogInterface.dismiss();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialogInterface.dismiss();
                                break;
                        }
                    }
                };
                builder.setPositiveButton(android.R.string.yes, listener);
                builder.setNegativeButton(android.R.string.no, listener);
                builder.setCancelable(false);
                Dialog g = builder.create();
                g.show();
                break;
            case android.R.id.home:
                Intent b=new Intent(SingleSubscriptionActivity.this, SubscriptionActivity.class);
                b.putExtra("SessionPosition", session_position);
                startActivity(b);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
