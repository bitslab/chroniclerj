package edu.columbia.cs.psl.chroniclerj.coordinate;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
                    switch (input.getClass().getSimpleName()) {
                        case "Integer":
                            repOut.writeInt((int) input);
                            repOut.flush();
                            break;
                        case "Float":
                            repOut.writeFloat((float) input);
                            repOut.flush();
                            break;
                        case "Short":
                            repOut.writeShort((short) input);
                            repOut.flush();
                            break;
                        case "Long":
                            repOut.writeLong((long) input);
                            repOut.flush();
                            break;
                        case "Boolean":
                            repOut.writeBoolean((boolean) input);
                            repOut.flush();
                            break;
                        case "Byte":
                            repOut.writeByte((byte) input);
                            repOut.flush();
                            break;
                        case "Char":
                            repOut.writeChar((char) input);
                            repOut.flush();
                            break;
                        case "Double":
                            repOut.writeDouble((double) input);
                            repOut.flush();
                            break;
                        default:
                            repOut.writeObject(input);
                            repOut.flush();
                            break;
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {
                //while (true) {}
                System.out.println("EOF Reached");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
