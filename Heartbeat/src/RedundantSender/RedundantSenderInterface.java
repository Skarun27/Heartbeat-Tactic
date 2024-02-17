package RedundantSender;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RedundantSenderInterface extends Remote{

    /* Activates the redundant sender module to send heartbeat to the receiver */
    void sendHeartBeat() throws IOException, InterruptedException;
    void monitorSenderModule() throws IOException, InterruptedException;
    void setLastHeartbeatTime() throws RemoteException;
}
