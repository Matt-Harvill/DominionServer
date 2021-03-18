package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Controller {

    @FXML private Text hostIP, portNum;

    public void initialize() throws UnknownHostException {
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        hostIP.setText(ipAddress);
        portNum.setText(String.valueOf(DominionHost.portNumber));
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
}
