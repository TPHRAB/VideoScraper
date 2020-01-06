package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Downloads {
    @FXML
    private VBox stage;

    @FXML
    void gotoHomeMenuClicked(ActionEvent event) throws Exception {
        loadScene("main.fxml");
    }

    private void loadScene(String scene) throws Exception {
        Stage primaryStage = (Stage) stage.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(scene));
        primaryStage.setScene(new Scene(root, 640, 400));
    }
}
