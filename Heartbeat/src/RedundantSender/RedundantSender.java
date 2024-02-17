package RedundantSender;

import FaultHandler.FaultMonitor;
import HeartbeatMonitor.ReceiverInterface;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/* Component under monitoring */
public class RedundantSender extends UnicastRemoteObject implements RedundantSenderInterface{

    private final int HEARTBEAT_INTERVAL = 2000;
    private static final int INTERVAL = 4000;
    private static final int expireInterval = 6000;
    private long lastHeartbeatTime;
    private Registry senderRegistry;
    private Registry receiverRegistry;
    private ReceiverInterface receiver_stub;

    public RedundantSender() throws RemoteException {
        super();
    }

    public void initialize(RedundantSender redundantSender) throws IOException, NotBoundException {

        try{
            senderRegistry = LocateRegistry.createRegistry(8080);
            senderRegistry.rebind("//localhost:8080/RedundantSender", redundantSender);

            /* gets the rmi registry process once initialized */
            receiverRegistry = LocateRegistry.getRegistry();
            /* Lookup registry to access the remote object of the monitoring component */
            receiver_stub = (ReceiverInterface) receiverRegistry.lookup("Receiver");

        }catch(Exception e){
            System.out.println("Redundant Sender: Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendHeartBeat() throws IOException, InterruptedException {
        try {
            Random random = new Random();
            int upperbound = 10;
            int intRandom = random.nextInt(upperbound);

            while(2500/intRandom > 0){
                System.out.println("Redundant Sender: I am Alive.");
                Thread.sleep(HEARTBEAT_INTERVAL);
                receiver_stub.setLastHeartbeatTime();
                intRandom = random.nextInt(upperbound);
            }
        }
        catch(ArithmeticException e) {
            System.out.println("********* Redundant Sender is now dead *********");
        }
    }

    /* monitors the time difference between the last 2 heart beat signals */
    public void monitorSenderModule() throws IOException, InterruptedException {
        while (isAlive()) {}
        sendHeartBeat();
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

    public void setLastHeartbeatTime() throws RemoteException{
        this.lastHeartbeatTime = System.currentTimeMillis();
        Date date = new Date(this.lastHeartbeatTime);
        System.out.println("New heartbeat sent at:" + date);
    }

    public static void main(String [] args) throws RemoteException {
        RedundantSender redundantSender = new RedundantSender();
        try{
            redundantSender.initialize(redundantSender);
            redundantSender.monitorSenderModule();

        }catch(NotBoundException | IOException | InterruptedException ex){
            ex.printStackTrace();
        }
    }
}