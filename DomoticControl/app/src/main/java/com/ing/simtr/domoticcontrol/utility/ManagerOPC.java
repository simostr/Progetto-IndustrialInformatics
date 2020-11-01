package com.ing.simtr.domoticcontrol.utility;


import android.content.Context;

import org.opcfoundation.ua.application.Application;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.ByteString;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.BrowseResultMask;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.transport.security.CertificateValidator;
import org.opcfoundation.ua.transport.security.KeyPair;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Stream;

public class ManagerOPC {

    //valori di default
    public final static Double DEFAULT_PUBLISHING_INTERVAL =new Double(1000);
    public final static UnsignedInteger DEFAULT_MAX_KEEP_ALIVE_COUNT =new UnsignedInteger(20);
    public final static UnsignedInteger DEFAULT_LIFETIME_COUNT =new UnsignedInteger(60);
    public final static UnsignedInteger DEFAULT_MAX_NOTIFICATION_FOR_PUBLISH =new UnsignedInteger(0);
    public final static UnsignedByte DEFAULT_PRIORITY=new UnsignedByte(0);
    //default monitored item
    public final static UnsignedInteger DEFAULT_QUEUE_SIZE =new UnsignedInteger(4);
    public final static Double DEFAULT_ABSOLUTE_DEAD_BAND =new Double(1);
    public final static Double DEFAULT_SAMPLING_INTERVAL =new Double(1000);
    //read parameter
    public final static Double DEFAULT_MAXAGE = new Double(500);

    private static ManagerOPC instance=null;
    private static Client client;
    private static Application clientApplication;
    private static KeyPair myClientAppCert;

    private List<SessionElementOPC> sessionsList;

    private List<NodeId> nodibase;
    private Stack<List<NodeId>> stack;

    private ManagerOPC() {

        nodibase=new ArrayList<>();
        nodibase.add(Identifiers.RootFolder);
        nodibase.add(Identifiers.ObjectsFolder);
        nodibase.add(Identifiers.TypesFolder);
        nodibase.add(Identifiers.ViewsFolder);
        nodibase.add(Identifiers.Server);

        sessionsList =new ArrayList<>();



        instance=this;
    }

    public static ManagerOPC CreateManagerOPC(final Context context){
        if(instance!=null){
            return instance;
        }

        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                clientApplication=new Application();

                ApplicationDescription applicationDescription=new ApplicationDescription();
                applicationDescription.setApplicationName(new LocalizedText(
                        "AndroidClientOPC", Locale.ITALIAN));
                applicationDescription.setApplicationUri("urn:localhost:AndroidClientOPC");
                applicationDescription.setProductUri("urn:simostr:AndroidClientOPC");
                applicationDescription.setApplicationType(ApplicationType.Client);

                //creo certificato
                try {
                    myClientAppCert= KeysGen.getCert(
                            context.getApplicationContext(), applicationDescription);
                } catch (ServiceResultException e) {
                    e.printStackTrace();
                }
                clientApplication.addApplicationInstanceCertificate(myClientAppCert);
                clientApplication.setApplicationUri("urn:localhost:AndroidClientOPC");
                clientApplication.setProductUri("urn:simostr:AndroidClientOPC");
                clientApplication.getOpctcpSettings().setCertificateValidator(CertificateValidator.ALLOW_ALL);
                client=new Client(clientApplication);
            }
        });
        t.start();
        try{
            t.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        instance=new ManagerOPC();
        return instance;
    }

    public static ManagerOPC getInstance(){
        if(instance==null)
            System.out.println("inizializza manager");
        return instance;
    }

    public Client getClient() {
        return client;
    }

    public int createSession(String url, EndpointDescription endpoint) throws ServiceResultException {
        SessionChannel tmp=client.createSessionChannel(url, endpoint);
        tmp.activate();
        sessionsList.add(new SessionElementOPC(tmp,url));
        return sessionsList.size()-1;
    }

    public List<SessionElementOPC> getSessionsList() {
        return sessionsList;
    }

    public NodeId getNode(int pos){
        return stack.peek().get(pos);
    }

    public void initStack(){
        stack=new Stack<>();
        stack.add(nodibase);
    }

    public void pop(){
        if(stack.size()>1)
            stack.pop();
    }

    public BrowseResponse Browse(int position, int session_position) throws ServiceResultException{
        BrowseDescription browse = new BrowseDescription();
        browse.setNodeId(ManagerOPC.getInstance().getNode(position));
        browse.setBrowseDirection(BrowseDirection.Forward);
        browse.setIncludeSubtypes(true);
        browse.setNodeClassMask(NodeClass.Object, NodeClass.Variable, NodeClass.Method);
        browse.setResultMask(BrowseResultMask.All);

        BrowseResponse res= sessionsList.get(session_position).getSession()
                .Browse(null, null, null, browse);
        ArrayList<NodeId> nodes= new ArrayList<>();

        for(int i=0;i<res.getResults().length;i++) {
            if (res.getResults()[i].getReferences() != null){
                for (int j = 0; j < res.getResults()[i].getReferences().length; j++) {
                    int namespace = res.getResults()[i].getReferences()[j].getNodeId().getNamespaceIndex();
                    NodeId node;

                    Object index=res.getResults()[i].getReferences()[j].getNodeId().getValue();
                    if (index instanceof String)
                        node = new NodeId(namespace, index.toString());
                    else if(index instanceof UnsignedInteger)
                        node = new NodeId(namespace, (UnsignedInteger) index);
                    else if(index instanceof UUID)
                        node = new NodeId(namespace, (UUID) index);
                    else if(index instanceof byte[])
                        node = new NodeId(namespace, (byte[]) index);
                    else if(index instanceof ByteString)
                        node = new NodeId(namespace, (ByteString) index);
                    else
                        node = new NodeId(namespace, (int) index);

                    nodes.add(node);
                }
            }
        }

        if(nodes.size()>0)
            stack.push(nodes);
        return res;
    }
}
