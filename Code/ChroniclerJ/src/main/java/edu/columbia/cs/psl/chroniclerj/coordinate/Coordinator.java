package edu.columbia.cs.psl.chroniclerj.coordinate;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import edu.columbia.cs.psl.chroniclerj.Log;
import edu.columbia.cs.psl.chroniclerj.xstream.StaticReflectionProvider;

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
    static XStream xstream = new XStream();
    static boolean flag = false;
    static boolean serialFlag = false;

    public static void _main() {
        try {
            ServerSocket recorder = null;
            Socket recorderSocket = null;
            ObjectOutputStream recOut = null;
            ObjectInputStream recIn = null;

            ServerSocket replayer = null;
            Socket replayerSocket = null;
            ObjectOutputStream repOut = null;

            try {
                recorder = new ServerSocket(1234);
                recorderSocket = recorder.accept();
                recOut = new ObjectOutputStream(recorderSocket.getOutputStream());
                recIn = new ObjectInputStream(recorderSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (flag) {
                try {
                    replayer = new ServerSocket(4231);
                    replayerSocket = replayer.accept();
                    repOut = new ObjectOutputStream(replayerSocket.getOutputStream());
                    repOut.writeBoolean(true);
                    repOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            recOut.writeBoolean(true);
            recOut.flush();

            try {
                while (true) {
                    Object input = recIn.readObject();
                    if (input instanceof String) {
                       System.out.println(input);
                    }
                    /*if (serialFlag) {
                        if (input != null && input.getClass().getSimpleName().equals("XMLAlert")) {
                            Object obj = xstream.fromXML((String) recIn.readObject());
                            if (obj == null) {
                                //System.out.println("NULL");
                            } else {
                                System.out.println(obj.getClass().getSimpleName());
                            }
                        }
                    }*/
                    /*if (flag) {
                        if (input == null) {
                            repOut.writeObject(null);
                        } else {
                            switch (input.getClass().getSimpleName()) {
                                case "XMLAlert":
                                    System.out.println(recIn.readObject());
                                    break;
                                case "Integer":
                                    repOut.writeInt((int) input);
                                    break;
                                case "Float":
                                    repOut.writeFloat((float) input);
                                    break;
                                case "Short":
                                    repOut.writeShort((short) input);
                                    break;
                                case "Long":
                                    repOut.writeLong((long) input);
                                    break;
                                case "Boolean":
                                    repOut.writeBoolean((boolean) input);
                                    break;
                                case "Byte":
                                    repOut.writeByte((byte) input);
                                    break;
                                case "Char":
                                    repOut.writeChar((char) input);
                                    break;
                                case "Double":
                                    repOut.writeDouble((double) input);
                                    break;
                                default:
                                    repOut.writeObject(input);
                                    break;
                            }
                        }*/
                    //repOut.flush();
                    //}
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
