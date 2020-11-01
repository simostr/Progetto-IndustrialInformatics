package com.ing.simtr.domoticcontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.simtr.domoticcontrol.utility.AdapterClass.BuffReadMonitoringAdapter;
import com.ing.simtr.domoticcontrol.utility.DelayedProgressDialog;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;
import com.ing.simtr.domoticcontrol.utility.MonitoredItemElementOPC;
import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;

import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;

public class MonitoringItemActivity extends AppCompatActivity {
    public final static int refreshrate=100;

    TextView textViewInfo;
    TextView textViewBuffSize;
    ListView listViewReadings;
    DelayedProgressDialog dialog;

    BuffReadMonitoringAdapter adapter;
    SessionElementOPC sessionElementOPC;
    int session_position;
    int sub_position;
    int mon_position;
    static boolean running=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_item);
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new DelayedProgressDialog(MonitoringItemActivity.this);

        session_position=getIntent().getIntExtra("SessionPosition", -1);
        sessionElementOPC= ManagerOPC.getInstance().getSessionsList().get(session_position);
        sub_position=getIntent().getIntExtra("subPosition", -1);
        mon_position=getIntent().getIntExtra("monPosition", -1);

        System.out.println("Debug: "+session_position+" : "+sub_position+" : "+mon_position+".");

        textViewInfo=findViewById(R.id.monitoreitem_buf_info_text);
        textViewBuffSize=findViewById(R.id.monitoreitem_buf_size_text);
        textViewBuffSize.setText(getString(R.string.ultime)+" "+MonitoredItemElementOPC.bufferSize+" "+getString(R.string.letture));
        //textViewBuffSize.setText(R.string.ultime+ MonitoredItemElementOPC.bufferSize +R.string.letture);
        listViewReadings=findViewById(R.id.list_buff_element);
        adapter=new BuffReadMonitoringAdapter(MonitoringItemActivity.this, R.layout.card_view_buffervalue_mon_i,
                            sessionElementOPC.getSubscriptionElementOPCS().get(sub_position).getMonitoredItemElementOPCS().get(mon_position).getReadings());
        listViewReadings.setAdapter(adapter);

        CreateMonitoredItemsResponse mi=sessionElementOPC.getSubscriptionElementOPCS().get(sub_position).getMonitoredItemElementOPCS().get(mon_position).getMonitoredItem();
        String text="Monitored Item ID: "+mi.getResults()[0].getMonitoredItemId()+"\n"+
                    "Sampling Interval: "+mi.getResults()[0].getRevisedSamplingInterval()+"\n"+
                    "Queue Size: "+mi.getResults()[0].getRevisedQueueSize();
        textViewInfo.setText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRunning(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getRunning()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    try {
                        Thread.sleep(refreshrate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setRunning(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.buf_mon_i_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                setRunning(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(getRunning()){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            try {
                                Thread.sleep(refreshrate);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                Toast.makeText(MonitoringItemActivity.this, R.string.buff_mon_i_aggiornamentoavviato, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_pause:
                Toast.makeText(MonitoringItemActivity.this, R.string.buff_mon_i_aggiornamentoinpausa, Toast.LENGTH_SHORT).show();
                setRunning(false);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static synchronized void setRunning(boolean running) {
        MonitoringItemActivity.running = running;
    }

    public static synchronized boolean getRunning(){
        return MonitoringItemActivity.running;
    }
}
