package com.ing.simtr.domoticcontrol;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.simtr.domoticcontrol.utility.AdapterClass.MonitoringAdapter;
import com.ing.simtr.domoticcontrol.utility.DelayedProgressDialog;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;
import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;


public class MonitoringActivity extends AppCompatActivity {

    TextView textViewSessionId;
    ListView listAllMonitoredItem;
    DelayedProgressDialog dialog;

    ManagerOPC managerOPC;
    SessionElementOPC sessionElementOPC;
    int session_position;
    MonitoringAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        setNavigation();
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new DelayedProgressDialog(MonitoringActivity.this);
        textViewSessionId=findViewById(R.id.txtSessionMonitoring);
        listAllMonitoredItem=findViewById(R.id.listMonitoraggio);

        managerOPC=ManagerOPC.getInstance();
        session_position=getIntent().getIntExtra("SessionPosition", -1);
        if(session_position<0){
            Toast.makeText(MonitoringActivity.this,R.string.ERROR ,Toast.LENGTH_LONG).show();
            finish();
        }

        sessionElementOPC=managerOPC.getSessionsList().get(session_position);
        textViewSessionId.setText("SessionID: "+sessionElementOPC.getSession().getSession().getName());

        adapter=new MonitoringAdapter(MonitoringActivity.this, R.layout.list_monitoring,
                    sessionElementOPC.getSubscriptionElementOPCS());
        listAllMonitoredItem.setAdapter(adapter);
    }

    private void setNavigation() {
        BottomNavigationView navigation=findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.nav_sensors);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_connection:
                        Intent a=new Intent(MonitoringActivity.this, SessionActivity.class);
                        a.putExtra("SessionPosition",session_position);
                        startActivity(a);
                        break;
                    case R.id.nav_subscription:
                        Intent b=new Intent(MonitoringActivity.this, SubscriptionActivity.class);
                        b.putExtra("SessionPosition",session_position);
                        startActivity(b);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        Thread.sleep(MonitoringItemActivity.refreshrate);
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
        MonitoringItemActivity.setRunning(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
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
                                Thread.sleep(MonitoringItemActivity.refreshrate);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                Toast.makeText(MonitoringActivity.this, R.string.buff_mon_i_aggiornamentoavviato, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_pause:
                Toast.makeText(MonitoringActivity.this, R.string.buff_mon_i_aggiornamentoinpausa, Toast.LENGTH_SHORT).show();
                setRunning(false);
                break;
            case android.R.id.home:
                Intent b=new Intent(MonitoringActivity.this, MainActivity.class);
                startActivity(b);
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
