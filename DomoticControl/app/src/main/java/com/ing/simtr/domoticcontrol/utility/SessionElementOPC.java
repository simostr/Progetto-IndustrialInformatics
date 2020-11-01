package com.ing.simtr.domoticcontrol.utility;

import com.ing.simtr.domoticcontrol.utility.ThreadClass.PublishThread;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;

import java.util.ArrayList;
import java.util.List;

public class SessionElementOPC {

    private SessionChannel session;
    private String url;
    private List<SubscriptionElementOPC> subscriptionElementOPCS;

    boolean running=false;
    PublishThread thread=null;

    public SessionElementOPC(SessionChannel session, String url) {
        this.session = session;
        this.url = url;
        subscriptionElementOPCS =new ArrayList<>();
    }

    public int CreateSubscription(CreateSubscriptionRequest request) throws ServiceResultException {
        CreateSubscriptionResponse response=session.CreateSubscription(request);

        if(thread!=null){
            try {
                stopRunning();
                thread.join();
                subscriptionElementOPCS.add(new SubscriptionElementOPC(response, this.session));
                startRunning();
                thread=new PublishThread(this);
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            subscriptionElementOPCS.add(new SubscriptionElementOPC(response, this.session));
            startRunning();
            thread=new PublishThread(this);
            thread.start();
        }

        return subscriptionElementOPCS.size()-1;
    }

    public synchronized void stopRunning(){
        running=false;
    }
    public synchronized void startRunning(){
        running=true;
    }
    public synchronized boolean isRunning(){
        return running;
    }

    public SessionChannel getSession() {
        return session;
    }

    public String getUrl() {
        return url;
    }

    public List<SubscriptionElementOPC> getSubscriptionElementOPCS() {
        return subscriptionElementOPCS;
    }
}
