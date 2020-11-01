package com.ing.simtr.domoticcontrol.utility.ThreadClass;

import com.ing.simtr.domoticcontrol.utility.ManagerOPC;
import com.ing.simtr.domoticcontrol.utility.SessionElementOPC;

import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.DataChangeNotification;
import org.opcfoundation.ua.core.MonitoredItemNotification;
import org.opcfoundation.ua.core.NotificationMessage;
import org.opcfoundation.ua.core.PublishResponse;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;

public class PublishThread extends Thread {

    SessionElementOPC sessionElementOPC;

    public PublishThread(SessionElementOPC sessionElementOPC) {
        this.sessionElementOPC = sessionElementOPC;
    }

    @Override
    public void run() {
        super.run();

        while (sessionElementOPC.isRunning()){
            if(sessionElementOPC.getSubscriptionElementOPCS().size()>0){
                PublishResponse publishResponse;
                try {
                    SubscriptionAcknowledgement[] subAck = new SubscriptionAcknowledgement[sessionElementOPC.getSubscriptionElementOPCS().size()];
                    for (int i = 0; i < sessionElementOPC.getSubscriptionElementOPCS().size(); i++) {
                        subAck[i] = sessionElementOPC.getSubscriptionElementOPCS().get(i).getSubAck();
                    }
                    publishResponse= sessionElementOPC.getSession().Publish(null, subAck);
                    for (int i = 0; i< sessionElementOPC.getSubscriptionElementOPCS().size(); i++){
                        if(sessionElementOPC.getSubscriptionElementOPCS().get(i).getSubscription().getSubscriptionId().getValue()==
                                publishResponse.getSubscriptionId().getValue()){
                            sessionElementOPC.getSubscriptionElementOPCS().get(i).setLastSequenceNumber(publishResponse.getNotificationMessage().getSequenceNumber());
                            NotificationMessage nm=publishResponse.getNotificationMessage();
                            ExtensionObject[] ex=nm.getNotificationData();
                            for(ExtensionObject ob : ex){
                                Object change=ob.decode(ManagerOPC.getInstance().getClient().getEncoderContext());
                                if(change instanceof DataChangeNotification){
                                    DataChangeNotification dataChange=(DataChangeNotification)change;
                                    MonitoredItemNotification[] mnchange=dataChange.getMonitoredItems();
                                    for(MonitoredItemNotification mItem: mnchange){
                                        for(int j = 0; j< sessionElementOPC.getSubscriptionElementOPCS().get(i).getMonitoredItemElementOPCS().size(); j++){
                                            if(mItem.getClientHandle().intValue()== sessionElementOPC.getSubscriptionElementOPCS().get(i).getMonitoredItemElementOPCS()
                                                    .get(j).getMonitoredItemRequest().getItemsToCreate()[0]
                                                    .getRequestedParameters().getClientHandle().intValue()){
                                                sessionElementOPC.getSubscriptionElementOPCS().get(i).getMonitoredItemElementOPCS().get(j).InsertNotification(mItem);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }catch(ServiceResultException e){
                    System.out.println("service result error");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
