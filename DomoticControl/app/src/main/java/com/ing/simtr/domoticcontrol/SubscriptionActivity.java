package com.ing.simtr.domoticcontrol;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ing.simtr.domoticcontrol.utility.AdapterClass.SubscriptionAdapter;
import com.ing.simtr.domoticcontrol.utility.DelayedProgressDialog;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;

public class SubscriptionActivity extends AppCompatActivity {

    ListView listSubscription;
    DelayedProgressDialog dialog;

    SubscriptionAdapter adapter;
    ManagerOPC managerOPC;
    int session_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        setNavigation();
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new DelayedProgressDialog(SubscriptionActivity.this);

        managerOPC=ManagerOPC.getInstance();
        session_position=getIntent().getIntExtra("SessionPosition", -1);
        if(session_position<0){
            Toast.makeText(SubscriptionActivity.this,R.string.ERROR ,Toast.LENGTH_LONG).show();
            finish();
        }

        listSubscription=findViewById(R.id.list_subscription);
        adapter=new SubscriptionAdapter(SubscriptionActivity.this, R.layout.card_view_subscription,
                managerOPC.getSessionsList().get(session_position).getSubscriptionElementOPCS());
        listSubscription.setAdapter(adapter);
        listSubscription.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(SubscriptionActivity.this, SingleSubscriptionActivity.class);
                intent.putExtra("SessionPosition", session_position);
                intent.putExtra("subPosition", position);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setNavigation() {
        BottomNavigationView navigation=findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.nav_subscription);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_connection:
                        Intent a=new Intent(SubscriptionActivity.this, SessionActivity.class);
                        a.putExtra("SessionPosition",session_position);
                        startActivity(a);
                        break;
                    case R.id.nav_sensors:
                        Intent b=new Intent(SubscriptionActivity.this, MonitoringActivity.class);
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
        adapter.notifyDataSetChanged();
        if(!ManagerOPC.getInstance().getSessionsList().get(session_position).getSession().getSecureChannel().isOpen()) {
            Intent i = new Intent(SubscriptionActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent b=new Intent(SubscriptionActivity.this, MainActivity.class);
                startActivity(b);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
