package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.Socket;
import java.net.ServerSocket;

public class ProcessIncomingRequest implements Runnable {
    private ServerSocket receiveSocket;

    public ProcessIncomingRequest(ServerSocket receiveSocket) {
        super();
        this.receiveSocket = receiveSocket;
    }

    @Override
    public void run() {
        String line;
        BufferedReader is;

        while (true) {
            try {
                Socket socket = receiveSocket.accept();

                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    
                while(true) {
                    line = is.readLine();
                    if(line == null) {
                        break;
                    }
                    System.out.println("Received: " + line);
                }
            } catch (SocketException e) {
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
