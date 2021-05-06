package edu.columbia.cs.psl.chroniclerj.coordinate;

import edu.columbia.cs.psl.chroniclerj.replay.ReplayRunner;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.IdentityHashMap;

public class Coordinator {

    //VARIABLES USED FOR MULTITHREADING IDEA -> ConnectionThread Class
    /*
    static volatile Socket recorderSocket;
    static volatile Socket replayerSocket;
    static volatile boolean recorderStatus = false;
    static volatile boolean replayerStatus = false;
     */

    static IdentityHashMap<Integer, Object> idMap = new IdentityHashMap<>();

    public static void _main() {
        try {
            ServerSocket recorder = new ServerSocket(1234);
            ServerSocket replayer = new ServerSocket(1235);
            Socket recorderSocket = recorder.accept();
            Socket replayerSocket = replayer.accept();
            OutputStream recOut = recorderSocket.getOutputStream();
            OutputStream repOut = replayerSocket.getOutputStream();
            BufferedWriter writeRec = new BufferedWriter(new OutputStreamWriter(recOut));
            BufferedWriter writeRep = new BufferedWriter(new OutputStreamWriter(repOut));

            writeRec.write("READY\n");
            writeRep.write("READY\n");
            writeRec.flush();
            writeRep.flush();

            ObjectInputStream recorderInput = new ObjectInputStream(recorderSocket.getInputStream());
            ObjectOutputStream replayerOutput = new ObjectOutputStream(repOut);

            try {
                while(true) {
                    replayerOutput.writeObject(recorderInput.readObject());
                    replayerOutput.flush();
                    System.out.println(recorderInput.readObject().toString());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {
                System.out.println("EOF Reached - Closing Files");
                recorderSocket.close();
                replayerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
