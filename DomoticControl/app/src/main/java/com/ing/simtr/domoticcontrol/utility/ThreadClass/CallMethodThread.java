package com.ing.simtr.domoticcontrol.utility.ThreadClass;

import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CallMethodRequest;
import org.opcfoundation.ua.core.CallRequest;
import org.opcfoundation.ua.core.CallResponse;

import java.util.ArrayList;

public class CallMethodThread extends Thread {

    Handler handler;
    int namespace;
    int nodeId;
    int o_namespace;
    int o_nodeId;
    ArrayList<String> input;
    String nodeId_String;

    SessionChannel session;
    boolean sent =false;

    public CallMethodThread(SessionChannel session, ArrayList<String> input, int namespace, int nodeId, int o_namespace, int o_nodeID){
        this.input=input;
        this.namespace=namespace;
        this.nodeId=nodeId;
        this.o_namespace=o_namespace;
        this.o_nodeId=o_nodeID;
        this.session=session;
        nodeId_String=null;
    }
    public CallMethodThread(SessionChannel session,int namespace, String nodeId, int o_namespace){
        this.namespace=namespace;
        this.nodeId_String=nodeId;

        this.session=session;
    }

    private synchronized void send(Message msg){
        if(!sent) {
            msg.sendToTarget();
            sent =true;
        }
    }

    public void start(Handler handler) {
        super.start();
        this.handler=handler;
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread t= new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        CallRequest req=new CallRequest();
                        CallMethodRequest[] callMethodRequests=new CallMethodRequest[1];
                        callMethodRequests[0]=new CallMethodRequest();
                        req.setMethodsToCall(callMethodRequests);
                        NodeId met=new NodeId(namespace, nodeId);
                        System.out.println(namespace+" "+nodeId);
                        callMethodRequests[0].setObjectId(new NodeId(o_namespace, o_nodeId));
                        callMethodRequests[0].setMethodId(met);
                        Variant[] vars=new Variant[input.size()];
                        for(int i=0; i<vars.length; i++){
                            vars[i]=new Variant(input.get(i));
                        }
                        callMethodRequests[0].setInputArguments(vars);
                        CallResponse res=session.Call(req);

                        send(handler.obtainMessage(0,res));
                    } catch (ServiceResultException e) {
                        send(handler.obtainMessage(-1,e.getStatusCode()));
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
