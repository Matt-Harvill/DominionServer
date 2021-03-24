package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private static DominionServer server;
    private static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dominionHostScene.fxml"));
        Parent root = loader.load();

        Parent gameInfoScene = FXMLLoader.load(getClass().getResource("gameInfoScene.fxml"));

        controller = loader.getController();
        primaryStage.setTitle("Dominion Server");
//        primaryStage.setScene(new Scene(root));
        primaryStage.setScene(new Scene(gameInfoScene));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            try {
                server.closeServerSocket();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    public static Controller getController() {
        return controller;
    }
    public static DominionServer getServer() {
        return server;
    }

    public static void main(String[] args) {
        Thread javaFXThread = new Thread(() -> launch(args));
        javaFXThread.start();

        server = new DominionServer();
        server.acceptConnections();
    }
}

class DominionServer {

    private ServerSocket serverSocket;
    private int numClients;
    private int portNumber;
    private String ipAddress;

    private List<ServerSideConnection> serverSideConnections;

    public DominionServer(){
        numClients = 0;
        serverSideConnections = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(0);
            ipAddress = InetAddress.getLocalHost().getHostAddress();
            portNumber = serverSocket.getLocalPort();
        } catch (Exception ex) {
            System.out.println("IOException from DominionServer()");
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Accepting connections...");

            do {
                Socket s = serverSocket.accept();
                numClients++;
                serverSideConnections.add(new ServerSideConnection(s));
                System.out.println("There are " + numClients + " clients connected");
            }
            while (!Main.getController().getGameStart());
            System.out.println("No longer accepting connections");
        } catch (IOException ex) {
            System.out.println("IOException from acceptConnections()");
        }
    }
    public void closeServerSocket() throws IOException {
        serverSocket.close();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
    public int getNumClients() {
        return numClients;
    }
    public int getPortNumber() {
        return portNumber;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public List<ServerSideConnection> getServerSideConnections() {
        return serverSideConnections;
    }
}

class ServerSideConnection implements Runnable {
    
    private Socket socket;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private SocketAddress clientIP;
    
    public ServerSideConnection(Socket s) {
        socket = s;
        clientIP = s.getRemoteSocketAddress();
        try {
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("IOException from SSC Constructor");
        }
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        while(true) {
            try {
                broadcast(receive());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String receive() throws IOException {
        return dataIn.readUTF();
    }
    public void broadcast(String s) throws IOException {
        for(ServerSideConnection ssc: Main.getServer().getServerSideConnections()) {
            if(ssc.equals(this)) continue;
            ssc.getDataOut().writeUTF(s);
        }
    }
    public DataOutputStream getDataOut() {
        return dataOut;
    }
}
