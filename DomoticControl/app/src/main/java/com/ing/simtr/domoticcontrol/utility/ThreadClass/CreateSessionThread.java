package com.ing.simtr.domoticcontrol.utility.ThreadClass;

import android.os.Handler;
import android.os.Message;

import com.ing.simtr.domoticcontrol.utility.ManagerOPC;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointDescription;


public class CreateSessionThread extends Thread {

    Handler handler;
    ManagerOPC managerOPC;
    EndpointDescription endpointDescription;
    String server_url;
    int position=-1;
    boolean sent=false;

    public CreateSessionThread(ManagerOPC managerOPC, EndpointDescription endpointDescription, String server_url) {
        this.managerOPC = managerOPC;
        this.endpointDescription = endpointDescription;
        this.server_url = server_url;
    }

    private synchronized void send(Message msg){
        if(!sent){
            msg.sendToTarget();
            sent=true;
        }
    }

    public synchronized void start(Handler handler) {
        super.start();
        this.handler=handler;
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        position=managerOPC.createSession(server_url, endpointDescription);
                        send(handler.obtainMessage(0, position));
                    } catch (ServiceResultException e) {
                        send(handler.obtainMessage(-1, e.getStatusCode()));
                    }
                }
            });
            t.start();
            t.join(8000);
            send(handler.obtainMessage(-2));
        } catch (InterruptedException e) {
            send(handler.obtainMessage(-2));
        }
    }
}
