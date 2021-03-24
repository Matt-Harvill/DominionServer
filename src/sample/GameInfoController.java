package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;

public class GameInfoController {

    @FXML
    private ComboBox<Integer> comboBox;
    @FXML private ChoiceBox<Integer> choiceBox;

    public void initialize() {
        choiceBox.getItems().add(2);
        choiceBox.getItems().add(3);
        choiceBox.getItems().add(4);
        choiceBox.getItems().add(5);
        choiceBox.getItems().add(6);
    }

    public void comboBoxAction(ActionEvent actionEvent) {
        System.out.println("comboBox pressed");
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        System.out.println("height " + choiceBox.getHeight() + ", width "+ choiceBox.getWidth());

    }
}
