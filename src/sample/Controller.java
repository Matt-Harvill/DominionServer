package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Controller {

    @FXML private Text hostIP, portNum;
    @FXML Button startGameButton;

    private String ipAddress; private int port;
    private boolean gameStart = false;

    public void initialize() {
        ipAddress = Main.getServer().getIpAddress();
        hostIP.setText(ipAddress);
        port = Main.getServer().getPortNumber();
        portNum.setText(String.valueOf(port));
    }

    public void hostIPCopyToClipboard(ActionEvent actionEvent) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(hostIP.getText());
        clipboard.setContent(content);
    }
    public void portNumCopyToClipboard(ActionEvent actionEvent) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(portNum.getText());
        clipboard.setContent(content);
    }

    public void startGame(ActionEvent actionEvent) throws IOException {
        gameStart = true;
        startGameButton.setDisable(true);

        Socket interruptSocket = new Socket(ipAddress,port);
        interruptSocket.close();
    }
    public boolean getGameStart() {
        return gameStart;
    }
}
