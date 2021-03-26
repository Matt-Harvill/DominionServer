package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class GameInfoController {

    @FXML private ChoiceBox<Integer> choiceBox;
    private int maxNumPlayers;

    public void initialize() {
        for(int i=2;i<=6;i++) {
            choiceBox.getItems().add(i);
        }
    }

    public void submitNumPlayers(ActionEvent actionEvent) throws IOException {
        maxNumPlayers = choiceBox.getValue();
        Main.startServer(maxNumPlayers);
    }
}
