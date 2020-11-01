package com.ing.simtr.domoticcontrol.utility;

import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.MonitoredItemNotification;

import java.util.LinkedList;

public class MonitoredItemElementOPC {

    private CreateMonitoredItemsResponse monitoredItem;
    private CreateMonitoredItemsRequest monitoredItemRequest;
    private LinkedList<MonitoredItemNotification> readings;
    public static final int bufferSize=5;

    public MonitoredItemElementOPC(CreateMonitoredItemsResponse monitoredItem, CreateMonitoredItemsRequest monitoredItemRequest) {
        this.monitoredItem = monitoredItem;
        this.monitoredItemRequest = monitoredItemRequest;

        readings=new LinkedList<>();
    }

    public CreateMonitoredItemsResponse getMonitoredItem() {
        return monitoredItem;
    }

    public CreateMonitoredItemsRequest getMonitoredItemRequest() {
        return monitoredItemRequest;
    }

    public LinkedList<MonitoredItemNotification> getReadings() {
        return readings;
    }

    public void InsertNotification(MonitoredItemNotification notification){
        if (readings.size()==bufferSize)
            readings.removeLast();
        readings.addFirst(notification);
    }
}
