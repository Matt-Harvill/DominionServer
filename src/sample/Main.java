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
import java.util.Scanner;

public class Main extends Application {

    private static DominionServer server;
    private static Controller serverController;
    private static Stage window;
    private static Parent serverInfoScene, gameInfoScene;

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;

        FXMLLoader loader = new FXMLLoader((getClass().getResource("gameInfoScene.fxml")));
        gameInfoScene = loader.load();
        window.setTitle("Select Number of Players");
        window.setScene(new Scene(gameInfoScene));
        window.show();

    }

    public static Controller getServerController() {
        return serverController;
    }
    public static DominionServer getServer() {
        return server;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void startServer(int maxNumPlayers) throws IOException {
        server = new DominionServer(maxNumPlayers);
        Thread serverAccepting = new Thread(() -> server.acceptConnections());
        serverAccepting.start();

        switchToServerInfoScene();
    }

    public static void switchToServerInfoScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("dominionHostScene.fxml"));
        serverInfoScene = loader.load();
        serverController = loader.getController();
        window.setTitle("Server Information");
        window.setScene(new Scene(serverInfoScene));
        window.show();
        Thread listenServerClose = new Thread(() -> window.setOnCloseRequest(e -> {
            try {
                server.closeServerSocket();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }));
        listenServerClose.start();
    }
}

class DominionServer {

    private ServerSocket serverSocket;
    private int numClients, portNumber, maxNumPlayers;
    private String ipAddress;

    private List<ServerSideConnection> serverSideConnections;

    public DominionServer(int maxNumPlayers){
        numClients = 0;
        this.maxNumPlayers = maxNumPlayers;
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
            while (!Main.getServerController().getGameStart() && numClients<maxNumPlayers);
            System.out.println("No longer accepting connections");

            int firstPlayerTurn = (int) (Math.random()*maxNumPlayers);
            if(serverSideConnections.get(firstPlayerTurn).getName()!=null) {
                serverSideConnections.get(firstPlayerTurn).broadcast("startTurn " + serverSideConnections.get(firstPlayerTurn).getName());
            } else {
                serverSideConnections.get(0).broadcast("startTurn " + serverSideConnections.get(0));
                System.out.println("player0 selected");
            }

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
    private String name;
    
    public ServerSideConnection(Socket s) {
        socket = s;
        name = "AnonymousPlayer" + Main.getServer().getNumClients();
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
                String receivedMessage = receive();
                String sendCommand = receivedMessage;
                Scanner scanner = new Scanner(receivedMessage);
                String receivedCommand = scanner.next();
                if(receivedCommand.equals("setName")) {
                    name = scanner.next();
                    sendCommand = "connected " + name;
                } else if(receivedCommand.equals("endTurn")) {
                    List<ServerSideConnection> connections = Main.getServer().getServerSideConnections();
                    int indexOfThis = connections.indexOf(this);
                    sendCommand = "startTurn ";
                    if(indexOfThis==connections.size()-1) {
                        sendCommand += connections.get(0).getName();
                    }
                    else sendCommand += connections.get(indexOfThis+1).getName();
                }
                broadcast(sendCommand);
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
    public String getName() {return name;}
}
