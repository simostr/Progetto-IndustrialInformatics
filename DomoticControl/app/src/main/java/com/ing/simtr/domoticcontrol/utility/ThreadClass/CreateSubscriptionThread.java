package com.ing.simtr.domoticcontrol.utility.ThreadClass;

import android.os.Handler;
import android.os.Message;

import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;

public class CreateSubscriptionThread extends Thread {

    Handler handler;
    SessionElementOPC sessionElementOPC;
    CreateSubscriptionRequest request;
    int position=-1;
    boolean sent=false;

    public CreateSubscriptionThread(SessionElementOPC sessionElementOPC, CreateSubscriptionRequest request) {
        this.sessionElementOPC = sessionElementOPC;
        this.request = request;
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
                        position= sessionElementOPC.CreateSubscription(request);
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
