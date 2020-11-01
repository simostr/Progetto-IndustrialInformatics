package com.ing.simtr.domoticcontrol.utility.ThreadClass;

import android.os.Handler;
import android.os.Message;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;

public class DeleteSubscriptionThread extends Thread {

    Handler handler;
    SessionChannel session;
    UnsignedInteger subid;
    boolean sent=false;

    public DeleteSubscriptionThread(SessionChannel session, UnsignedInteger subid) {
        this.session = session;
        this.subid = subid;
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
                        session.DeleteSubscriptions(null, subid);
                        send(handler.obtainMessage(0));
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
