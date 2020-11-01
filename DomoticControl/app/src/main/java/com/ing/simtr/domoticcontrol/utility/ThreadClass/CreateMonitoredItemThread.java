package com.ing.simtr.domoticcontrol.utility.ThreadClass;

import android.os.Handler;
import android.os.Message;

import com.ing.simtr.domoticcontrol.utility.Exception.MICreateException;
import com.ing.simtr.domoticcontrol.utility.SubscriptionElementOPC;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;

public class CreateMonitoredItemThread extends Thread {

    Handler handler;
    SubscriptionElementOPC subscriptionElementOPC;
    CreateMonitoredItemsRequest request;
    int position=-1;
    boolean sent=false;

    public CreateMonitoredItemThread(SubscriptionElementOPC subscriptionElementOPC, CreateMonitoredItemsRequest request) {
        this.subscriptionElementOPC = subscriptionElementOPC;
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
                        position= subscriptionElementOPC.CreatMonitoredItem(request);
                        send(handler.obtainMessage(0, position));
                    } catch (ServiceResultException e) {
                        send(handler.obtainMessage(-1, e.getStatusCode()));
                    } catch (MICreateException e) {
                        send(handler.obtainMessage(-3, e.getMessage()));
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
