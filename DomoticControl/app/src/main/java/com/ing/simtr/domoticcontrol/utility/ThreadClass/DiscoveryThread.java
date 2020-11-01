package com.ing.simtr.domoticcontrol.utility.ThreadClass;

import android.os.Handler;
import android.os.Message;

import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointDescription;



public class DiscoveryThread extends Thread {

    Handler handler;
    String server_url;
    Client client;
    boolean sent=false;

    public DiscoveryThread(String server_url, Client client) {
        this.server_url = server_url;
        this.client=client;
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
                        EndpointDescription[] endpointDescriptions = client.discoverEndpoints(server_url);
                        send(handler.obtainMessage(0, endpointDescriptions));
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
