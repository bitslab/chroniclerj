/*
package edu.columbia.cs.psl.chroniclerj.coordinate;

import edu.columbia.cs.psl.chroniclerj.coordinate.Coordinator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//USED FOR MULTITHREADING IN COORDINATOR
//BENCHED FOR NOW MIGHT BE USEFUL IN FUTURE
public class ConnectionThread extends Thread {
    private ServerSocket serverSocket;

    public ConnectionThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            if (reader.readLine() == "RECORD") {
                Coordinator.recorderSocket = socket;
                Coordinator.recorderStatus = true;
            } else if (reader.readLine() == "REPLAY") {
                Coordinator.replayerSocket = socket;
                Coordinator.replayerStatus = true;
            }
            while (!Coordinator.recorderStatus && !Coordinator.replayerStatus) {}
            writer.write("READY\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/
