package com.ing.simtr.domoticcontrol;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.simtr.domoticcontrol.utility.DelayedProgressDialog;
import com.ing.simtr.domoticcontrol.utility.ManagerOPC;
import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;
import com.ing.simtr.domoticcontrol.utility.ThreadClass.CreateSubscriptionThread;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;

public class SessionActivity extends AppCompatActivity implements View.OnClickListener {

    TextView txtUrl;
    TextView txtSecurity;
    TextView txtSessionId;
    DelayedProgressDialog dialog;
    Button createSubscriptionButton;
    Button browsingButton;

    int session_position;

    ManagerOPC managerOPC;
    SessionElementOPC sessionElementOPC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        setNavigation();
        Toolbar myToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new DelayedProgressDialog(SessionActivity.this);

        txtUrl=findViewById(R.id.session_a_url);
        txtSessionId=findViewById(R.id.session_a_sessionid);
        txtSecurity=findViewById(R.id.session_a_securittext);
        createSubscriptionButton=findViewById(R.id.button_createsubscription);
        browsingButton=findViewById(R.id.button_browsing);

        session_position=getIntent().getIntExtra("SessionPosition", -1);

        if(session_position<0){
            Toast.makeText(SessionActivity.this, R.string.ERROR, Toast.LENGTH_SHORT).show();
            finish();
        }

        managerOPC=ManagerOPC.getInstance();
        sessionElementOPC=managerOPC.getSessionsList().get(session_position);

        txtUrl.setText(sessionElementOPC.getUrl());
        txtSessionId.setText(sessionElementOPC.getSession().getSession().getName());
        txtSecurity.setText(sessionElementOPC.getSession().getSession().getEndpoint().getSecurityMode().toString());

        createSubscriptionButton.setOnClickListener(this);
        browsingButton.setOnClickListener(this);
    }

    private void setNavigation() {
        BottomNavigationView navigation=findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.nav_connection);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_subscription:
                        Intent a=new Intent(SessionActivity.this, SubscriptionActivity.class);
                        a.putExtra("SessionPosition",session_position);
                        startActivity(a);
                        break;
                    case R.id.nav_sensors:
                        Intent b=new Intent(SessionActivity.this, MonitoringActivity.class);
                        b.putExtra("SessionPosition",session_position);
                        startActivity(b);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_createsubscription:
                final Dialog dialog_subscription=new Dialog(SessionActivity.this,
                        R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                dialog_subscription.setContentView(R.layout.dialog_create_subscription);

                final EditText edtPublishingInterval=dialog_subscription.findViewById(R.id.dialog_sub_pubin);
                final EditText edtMaxKeepAliveCount=dialog_subscription.findViewById(R.id.dialog_sub_keepalive);
                final EditText edtLifeTimeCount=dialog_subscription.findViewById(R.id.dialog_sub_lifetime);
                final EditText edtMaxNotificationForPublish=dialog_subscription.findViewById(R.id.dialog_sub_notforpub);
                final EditText edtPriority=dialog_subscription.findViewById(R.id.dialog_sub_priority);
                final CheckBox checkPublishingEnable=dialog_subscription.findViewById(R.id.dialog_sub_checkPub);
                Button btnCreateSub=dialog_subscription.findViewById(R.id.dialog_sub_ok);

                edtPublishingInterval.setText(ManagerOPC.DEFAULT_PUBLISHING_INTERVAL.toString());
                edtPublishingInterval.setHint("Es: "+ManagerOPC.DEFAULT_PUBLISHING_INTERVAL);
                edtMaxKeepAliveCount.setText(ManagerOPC.DEFAULT_MAX_KEEP_ALIVE_COUNT.toString());
                edtMaxKeepAliveCount.setHint("Es: "+ManagerOPC.DEFAULT_MAX_KEEP_ALIVE_COUNT);
                edtLifeTimeCount.setText(ManagerOPC.DEFAULT_LIFETIME_COUNT.toString());
                edtLifeTimeCount.setHint("Es: "+ManagerOPC.DEFAULT_LIFETIME_COUNT);
                edtMaxNotificationForPublish.setText(ManagerOPC.DEFAULT_MAX_NOTIFICATION_FOR_PUBLISH.toString());
                edtMaxNotificationForPublish.setHint("Es: "+ManagerOPC.DEFAULT_MAX_NOTIFICATION_FOR_PUBLISH);
                edtPriority.setText(ManagerOPC.DEFAULT_PRIORITY.toString());
                edtPriority.setHint("Es: "+ManagerOPC.DEFAULT_PRIORITY);

                btnCreateSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Double pubIn;
                        UnsignedInteger lifeCo;
                        UnsignedInteger keepAl;
                        UnsignedInteger notForP;
                        UnsignedByte prio;
                        boolean pubEnable;

                        String publishingInterval=edtPublishingInterval.getText().toString();
                        String maxKeepAlive=edtMaxKeepAliveCount.getText().toString();
                        String lifetime=edtLifeTimeCount.getText().toString();
                        String maxNotification=edtMaxNotificationForPublish.getText().toString();
                        String priority=edtPriority.getText().toString();

                        if(publishingInterval.length()==0 || maxKeepAlive.length()==0 ||
                                lifetime.length()==0 || maxNotification.length()==0 || priority.length()==0){

                            Toast.makeText(SessionActivity.this,
                                    R.string.dialog_sub_enterValidValue, Toast.LENGTH_LONG).show();

                        }else{
                            pubIn=new Double(publishingInterval);
                            lifeCo=new UnsignedInteger(lifetime);
                            keepAl=new UnsignedInteger(maxKeepAlive);
                            notForP=new UnsignedInteger(maxNotification);
                            prio=new UnsignedByte(priority);
                            pubEnable=checkPublishingEnable.isChecked();
                            if(lifeCo.intValue()>=(3*keepAl.intValue())){
                                CreateSubscriptionRequest req=new CreateSubscriptionRequest(
                                        null, pubIn, lifeCo, keepAl, notForP, pubEnable, prio);
                                CreateSubscriptionThread t=new CreateSubscriptionThread(sessionElementOPC, req);
                                dialog.show(getSupportFragmentManager(), getString(R.string.attendi));
                                Handler handler_subscription=new Handler(){
                                    @Override
                                    public void handleMessage(Message msg) {
                                        dialog.cancel();
                                        if(msg.what==-1){
                                            Toast.makeText(SessionActivity.this,getString(R.string.creazione_subfallita)+((StatusCode)msg.obj).getDescription()+
                                                            "\nCode: "+((StatusCode)msg.obj).getValue().toString(),
                                                    Toast.LENGTH_LONG).show();
                                        }else if(msg.what==-2){
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.request_timeout,Toast.LENGTH_LONG).show();
                                        } else{
                                            int position= (int)msg.obj;
                                            Toast.makeText(SessionActivity.this,
                                                    "session "+position+" create", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                dialog_subscription.dismiss();
                                t.start(handler_subscription);
                            }else {
                                Toast.makeText(SessionActivity.this,
                                        R.string.dialog_sub_rispettaVincoli, Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });
                dialog_subscription.show();
                break;
            case R.id.button_browsing:
                Intent intent=new Intent(SessionActivity.this, BrowsingActivity.class);
                intent.putExtra("SessionPosition", session_position);
                startActivity(intent);
                break;
        }
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
            case R.id.session_menu_terminate:
                AlertDialog.Builder builder = new AlertDialog.Builder(SessionActivity.this);
                builder.setTitle(R.string.closeSession);
                builder.setMessage(R.string.closeSessionMessage);
                DialogInterface.OnClickListener listener= new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                managerOPC.getSessionsList().remove(session_position);
                                sessionElementOPC.getSession().closeAsync();
                                dialogInterface.dismiss();
                                finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialogInterface.dismiss();
                                break;
                        }
                    }
                };
                builder.setPositiveButton(android.R.string.yes,listener);
                builder.setNegativeButton(android.R.string.no,listener);
                builder.setCancelable(false);
                Dialog g= builder.create();
                g.show();
                break;
            case R.id.session_menu_sub:
                Intent a=new Intent(SessionActivity.this, SubscriptionActivity.class);
                a.putExtra("SessionPosition",session_position);
                startActivity(a);
                break;
            case android.R.id.home:
                Intent b=new Intent(SessionActivity.this, MainActivity.class);
                startActivity(b);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!ManagerOPC.getInstance().getSessionsList().get(session_position).getSession().getSecureChannel().isOpen()){
            Intent i=new Intent(SessionActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

}
