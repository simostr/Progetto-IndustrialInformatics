package com.ing.simtr.domoticcontrol.utility;

import com.ing.simtr.domoticcontrol.utility.Exception.MICreateException;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionElementOPC {

    private CreateSubscriptionResponse subscription;
    private List<MonitoredItemElementOPC> monitoredItemElementOPCS;
    private SessionChannel sessionChannel;

    SubscriptionAcknowledgement subAck;

    public SubscriptionElementOPC(CreateSubscriptionResponse subscription, SessionChannel sessionChannel) {
        this.subscription = subscription;
        this.sessionChannel = sessionChannel;

        this.monitoredItemElementOPCS =new ArrayList<>();

        subAck=new SubscriptionAcknowledgement();
        subAck.setSubscriptionId(new UnsignedInteger(subscription.getSubscriptionId()));
    }

    public SubscriptionAcknowledgement getSubAck() {
        return subAck;
    }

    public void setLastSequenceNumber(UnsignedInteger lastSequenceNumber){
        subAck.setSequenceNumber(lastSequenceNumber);
    }

    public CreateSubscriptionResponse getSubscription() {
        return subscription;
    }

    public List<MonitoredItemElementOPC> getMonitoredItemElementOPCS() {
        return monitoredItemElementOPCS;
    }

    public SessionChannel getSessionChannel() {
        return sessionChannel;
    }

    public int CreatMonitoredItem(CreateMonitoredItemsRequest morequest)
        throws ServiceResultException, MICreateException {

        CreateMonitoredItemsResponse response=sessionChannel.CreateMonitoredItems(morequest);
        if(response.getResults()[0].getStatusCode().getValue().intValue()!= StatusCode.GOOD.getValue().intValue()){
            throw new MICreateException(response.getResults()[0].getStatusCode().getDescription());

        }
        monitoredItemElementOPCS.add(new MonitoredItemElementOPC(response, morequest));
        return monitoredItemElementOPCS.size()-1;
    }
}
