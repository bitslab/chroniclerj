package edu.columbia.cs.psl.chroniclerj.coordinate;

import edu.columbia.cs.psl.chroniclerj.replay.ReplayRunner;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Coordinator {

    //VARIABLES USED FOR MULTITHREADING IDEA -> ConnectionThread Class
    /*
    static volatile Socket recorderSocket;
    static volatile Socket replayerSocket;
    static volatile boolean recorderStatus = false;
    static volatile boolean replayerStatus = false;
     */

    public static void _main(String[] classpath) {
        try {
            ServerSocket recorder = new ServerSocket(1234);
            ServerSocket replayer = new ServerSocket(1235);
            Socket recorderSocket = recorder.accept();
            //Socket replayerSocket = replayer.accept();
            BufferedWriter recOut = new BufferedWriter(new OutputStreamWriter(recorderSocket.getOutputStream()));
            //BufferedWriter repOut = new BufferedWriter(new OutputStreamWriter(replayerSocket.getOutputStream()));
            recOut.write("READY\n");
            recOut.flush();
            //repOut.write("READY\n");
            DataInputStream recorderInput = new DataInputStream(recorderSocket.getInputStream());
            /*File file = new File("/Users/david/IdeaProjects/chroniclerj/Code/temp.test");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            int count;
            while ((count = recorderInput.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
            fos.close();*/

            try {
                System.out.println(recorderInput.readInt());
            } catch (IOException e) {
                e.printStackTrace();
            }
        recorderSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //classpath[1] = "/Users/david/IdeaProjects/chroniclerj/Code/temp.test";
        //ReplayRunner._main(classpath);
    }
}
