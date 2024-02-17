package Sender;

import HeartbeatMonitor.ReceiverInterface;
import RedundantSender.RedundantSenderInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

/* Component under monitoring */
public class Sender {

    private final int HEARTBEAT_INTERVAL = 2000;
    private Registry registry;
    private Registry senderRegistry;
    private ReceiverInterface receiver_stub;
    private static RedundantSenderInterface redundantSender_stub;

    public void initialize() throws IOException, NotBoundException {

        /* gets the rmi registry process once initialized */
        registry = LocateRegistry.getRegistry();

        /* Lookup registry to access the remote object of the monitoring component */
        receiver_stub = (ReceiverInterface) registry.lookup("Receiver");

        /* gets the rmi registry process once initialized */
        senderRegistry = LocateRegistry.getRegistry(8080);

        /* Lookup registry to access the remote object of the monitoring component */
        redundantSender_stub = (RedundantSenderInterface) senderRegistry.lookup("//localhost:8080/RedundantSender");
    }

    public void sendHeartBeat() throws IOException, InterruptedException {
        try {
            Random random = new Random();
            int upperbound = 10;
            int intRandom = random.nextInt(upperbound);

            while(2500/intRandom > 0){
                System.out.println("Sender: I am Alive.");
                Thread.sleep(HEARTBEAT_INTERVAL);
                receiver_stub.setLastHeartbeatTime();
                redundantSender_stub.setLastHeartbeatTime();
                intRandom = random.nextInt(upperbound);
            }
        }
        catch(ArithmeticException | InterruptedException e) {
            System.out.println("********* Sender is now dead *********");
        }
    }

    public static void main(String [] args) throws IOException, InterruptedException {
        Sender sender = new Sender();
        try{
            sender.initialize();
            sender.sendHeartBeat();

        }catch(NotBoundException | IOException | InterruptedException ex){
            redundantSender_stub.sendHeartBeat();
            ex.printStackTrace();
        }
    }
}
