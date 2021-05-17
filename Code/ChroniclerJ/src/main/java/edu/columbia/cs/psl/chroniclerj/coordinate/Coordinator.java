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

    public static void _main() {
        try {
            ServerSocket recorder = new ServerSocket(1234);
            ServerSocket replayer = new ServerSocket(1235);
            Socket recorderSocket = recorder.accept();
            Socket replayerSocket = replayer.accept();
            ObjectOutputStream recOut = new ObjectOutputStream(recorderSocket.getOutputStream());
            ObjectOutputStream repOut = new ObjectOutputStream(replayerSocket.getOutputStream());
            ObjectInputStream recIn = new ObjectInputStream(recorderSocket.getInputStream());

            recOut.writeBoolean(true);
            repOut.writeBoolean(true);
            recOut.flush();
            repOut.flush();

            try {
                while(true) {
                    Object input = recIn.readObject();
                    if (input.getClass() == Integer.class){
                        repOut.writeInt((int) input);
                        repOut.flush();
                        System.out.println(input.toString());
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {
                while (true) {}
                //System.out.println("EOF Reached");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
