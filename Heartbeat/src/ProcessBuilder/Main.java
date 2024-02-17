package ProcessBuilder;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {

    public static void main(String [] args){
        File currentDirFile = new File(".");
        String helper = currentDirFile.getAbsolutePath();
        
        try {
            // Initializes the process
            ProcessBuilder pb = new ProcessBuilder("rmiregistry");
            pb.directory(new File("." + File.separator +"out" + File.separator +"production"+ File.separator +"Heartbeat"));
            pb.start();

            Thread.sleep(1000);

            // Intializing Receiver (Monitoring Module)
            System.out.println("Initializing receiver");
            ProcessBuilder receiver_builder = new ProcessBuilder("java" , "-cp",
                    helper + File.separator + "out"+ File.separator +"production" + File.separator +"Heartbeat" + File.separator ,
                    "HeartbeatMonitor.Receiver");
            receiver_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process heartbeatMonitor = receiver_builder.start();
            System.out.println("Receiver Initialized");

            Thread.sleep(1000);

            // Intializing Redundant Sender (Module Monitored)
            System.out.println("Initializing redundant sender");
            ProcessBuilder redundantSender_builder = new ProcessBuilder("java", "-cp",
                    helper + File.separator + "out"+ File.separator +"production" + File.separator +"Heartbeat" + File.separator ,
                    "RedundantSender.RedundantSender");
            redundantSender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process redundantSenderModule = redundantSender_builder.start();
            System.out.println("Redundant Sender Initialized");

            Thread.sleep(1000);

            // Intializing Sender (Module Monitored)
            System.out.println("Initializing sender");
            ProcessBuilder sender_builder = new ProcessBuilder("java", "-cp",
                    helper + File.separator + "out"+ File.separator +"production" + File.separator +"Heartbeat" + File.separator ,
                    "Sender.Sender");
            sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process senderModule = sender_builder.start();
            System.out.println("Sender Initialized");

            Thread.sleep(1000);

            // Prints error if any, while initializing the receiver module
            InputStream error_receiver = heartbeatMonitor.getErrorStream();
            StringBuilder receiverError = new StringBuilder();
            receiverError.append("Receiver : ");
            if(heartbeatMonitor.getErrorStream().read() != -1){
                for (int i = 0; i < error_receiver.available(); i++) {
                    receiverError.append((char)error_receiver.read());
                }
                System.out.println(receiverError.toString());
            }else{
                System.out.println(heartbeatMonitor.getOutputStream());
            }

            // Prints error if any, while initializing the sender module
            InputStream error_sender = senderModule.getErrorStream();
            StringBuilder senderError = new StringBuilder();
            senderError.append("Sender : ");
            if(senderModule.getErrorStream().read() == -1) {
                for (int i = 0; i < error_sender.available(); i++) {
                    senderError.append((char) error_sender.read() + " ");
                }
                System.out.println(senderError.toString());
            }else{
                OutputStream out = senderModule.getOutputStream();
                System.out.println(out);
            }

            // Prints error if any, while initializing the redundant sender module
            InputStream error_redundantSender = redundantSenderModule.getErrorStream();
            StringBuilder redundantSenderError = new StringBuilder();
            redundantSenderError.append("Redundant Sender : ");
            if(redundantSenderModule.getErrorStream().read() == -1) {
                for (int i = 0; i < error_redundantSender.available(); i++) {
                    redundantSenderError.append((char) error_redundantSender.read() + " ");
                }
                System.out.println(redundantSenderError.toString());
            }else{
                OutputStream out = redundantSenderModule.getOutputStream();
                System.out.println(out);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
