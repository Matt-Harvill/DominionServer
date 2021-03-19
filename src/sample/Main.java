package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main extends Application {

    private static DominionServer server;
    private static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dominionHostScene.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setTitle("Dominion Server");
        primaryStage.setScene(new Scene(root));
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
    private int numPlayers;
    private int portNumber;
    private String ipAddress;

    public DominionServer(){
        numPlayers = 0;
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
                numPlayers++;
                System.out.println(s.getLocalSocketAddress());
            }
            while (!Main.getController().getGameStart());

            System.out.println("No longer accepting connections");
            System.out.println((numPlayers-1) + " players connected");
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
    public int getNumPlayers() {
        return numPlayers;
    }
    public int getPortNumber() {
        return portNumber;
    }
    public String getIpAddress() {
        return ipAddress;
    }
}
