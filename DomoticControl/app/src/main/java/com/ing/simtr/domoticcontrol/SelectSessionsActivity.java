package com.ing.simtr.domoticcontrol;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ing.simtr.domoticcontrol.utility.AdapterClass.SessionAdapter;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;

public class SelectSessionsActivity extends AppCompatActivity {

    ListView listSessions;
    ManagerOPC managerOPC;
    SessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sessions);
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        managerOPC=ManagerOPC.getInstance();

        listSessions=findViewById(R.id.list_sessions);
        adapter=new SessionAdapter(this, R.layout.card_view_session, managerOPC.getSessionsList());
        listSessions.setAdapter(adapter);

        listSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(SelectSessionsActivity.this, SessionActivity.class);
                intent.putExtra("SessionPosition", position);
                intent.putExtra("Url", managerOPC.getSessionsList().get(position).getUrl());
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent b=new Intent(SelectSessionsActivity.this, MainActivity.class);
                startActivity(b);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
