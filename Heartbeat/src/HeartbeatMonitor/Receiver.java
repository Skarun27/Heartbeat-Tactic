package HeartbeatMonitor;

import FaultHandler.FaultMonitor;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;


public class Receiver extends UnicastRemoteObject implements ReceiverInterface{

    private static final int INTERVAL = 4000;
    private static final int expireInterval = 10000;
    private static final String HOST = "localhost";
    private long lastHeartbeatTime;


    protected Receiver() throws RemoteException {
        super();
    }

    public void initializeReceiver(Receiver receiver){
       Registry registry;
        try{
            registry = LocateRegistry.getRegistry(HOST);
            registry.rebind("Receiver", receiver);

        }catch(Exception e){
            System.out.println(" Receiver: Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* monitors the time difference between the last 2 heart beat signals */
    public void monitorSenderModule() throws RemoteException, InterruptedException {
        while (isAlive()) {
            read();
        }
        System.out.println("HearBeat wait time exceeded - Sender Component failed - Check logs for details");
        FaultMonitor.handleFault("Sender");
    }

    /* Checks if the sender is alive*/
    private boolean isAlive() throws InterruptedException {
        Thread.sleep(INTERVAL);
        long interval=0;
        if(this.lastHeartbeatTime != 0) {
            interval = System.currentTimeMillis() - lastHeartbeatTime;
            return (expireInterval) >= interval;
        }
        return true;
    }

    /*Receives heart beat messages from the monitored component*/
    public void read() throws RemoteException{
        Date date = new Date(this.lastHeartbeatTime);
        System.out.println("Receiver: Acknowledged last heartbeat at : " + date);
    }

    public void setLastHeartbeatTime() throws RemoteException{
        this.lastHeartbeatTime = System.currentTimeMillis();
        Date date = new Date(this.lastHeartbeatTime);
        System.out.println("New heartbeat sent at:" + date);
    }

    public static void main(String [] args) throws RemoteException {
        Receiver receiver = new Receiver();
        receiver.initializeReceiver(receiver);
        try{
            receiver.monitorSenderModule();
        }catch(Exception ex){
            System.out.println("HearBeat Monitor: Module exception  - " + ex.getMessage());
        }
    }
}
