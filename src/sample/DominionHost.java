package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DominionHost extends Application {

    public static int portNumber;
    private static DominionServer server;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
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


    public static void main(String[] args) {
        new Thread(() -> {
            launch(args);
        }).start();
        server = new DominionServer();
        server.acceptConnections();

    }
}

class DominionServer {

    private ServerSocket serverSocket;

    public DominionServer(){
        try {
            serverSocket = new ServerSocket(0);
            DominionHost.portNumber = serverSocket.getLocalPort();
        } catch (IOException ex) {
            System.out.println("IOException from DominionServer()");
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Accepting players...");
            while (true) {
                Socket s = serverSocket.accept();
                break;
            }
            System.out.println("Player connected!");
        } catch (IOException ex) {
            System.out.println("DominionServer Shut Down");
        }
    }

    public void closeServerSocket() throws IOException {
        serverSocket.close();
    }
}
