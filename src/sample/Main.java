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
    public int getMaxNumPlayers() {return maxNumPlayers;}
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
    private int points;
    private String playerInfoString;
    
    public ServerSideConnection(Socket s) {
        socket = s;
        name = "AnonymousPlayer" + Main.getServer().getNumClients();
        points = 0;
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
                String sendMessage = receivedMessage;
                Scanner scanner = new Scanner(receivedMessage);
                String command = scanner.next();

                if(command.equals("join")) {
                    name = scanner.next();
                    points = scanner.nextInt();
                    sendMessage = "inGame ";

                    DominionServer server = Main.getServer();
                    List<ServerSideConnection> serverSideConnections = server.getServerSideConnections();

                    for(ServerSideConnection ssc: serverSideConnections) {
                        if(ssc.equals(this)) continue;
                        sendMessage+=ssc.getPlayerInfoString();
                        individualSend(sendMessage);
                        sendMessage = "inGame ";
                    }
                    sendMessage = "connected " + getPlayerInfoString();

                    if(server.getNumClients()==server.getMaxNumPlayers()) {
                        int firstPlayerTurn = (int) (Math.random()*server.getMaxNumPlayers());
                        if(serverSideConnections.get(firstPlayerTurn).getName()!=null) {
                            serverSideConnections.get(firstPlayerTurn).broadcastAll(
                                    "startTurn " + serverSideConnections.get(firstPlayerTurn).getPlayerInfoString());
                        } else {
                            serverSideConnections.get(0).broadcastAll(
                                    "startTurn " + serverSideConnections.get(0).getPlayerInfoString());
                            System.out.println("player0 selected");
                        }
                    }
                }

                else if(command.equals("endTurn")) {
                    List<ServerSideConnection> connections = Main.getServer().getServerSideConnections();
                    int indexOfThis = connections.indexOf(this);
                    sendMessage = "startTurn ";
                    if(indexOfThis==connections.size()-1) {
                        sendMessage+=connections.get(0).getPlayerInfoString();
                    }
                    else sendMessage+=connections.get(indexOfThis + 1).getPlayerInfoString();
                    individualSend(sendMessage);
                }

                broadcast(sendMessage);

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
            ssc.getDataOut().flush();
        }
    }
    public void broadcastAll(String s) throws IOException {
        for(ServerSideConnection ssc: Main.getServer().getServerSideConnections()) {
            ssc.getDataOut().writeUTF(s);
            ssc.getDataOut().flush();
        }
    }
    public void individualSend(String s) throws IOException {
        dataOut.writeUTF(s);
        dataOut.flush();
    }

    public DataOutputStream getDataOut() {
        return dataOut;
    }

    public String getPlayerInfoString() {
        playerInfoString = name + " " + points + " ";
        return playerInfoString;
    }

    public String getName() {return name;}
    public int getPoints() {
        return points;
    }
}
