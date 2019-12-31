package sample;

import download2.DownloadModule;
import extensions.dom4j.Dom4jUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.dom4j.Document;

import java.util.List;

public class MethodsController {
    @FXML
    private VBox stage;
    @FXML
    private MenuItem gotoHomeMenu;

    @FXML
    private ListView<String> methodsList;

    @FXML
    private AnchorPane informationPanel;

    /**
     * initialize the class after loading methods.fxml
     */
    @FXML
    private void initialize() {
        Document xml = Dom4jUtil.getDocument("modules.xml");
        // set cell look
        methodsList.setCellFactory(new Callback<ListView<String>,
                                            ListCell<String>>() {
                                @Override
                                public ListCell<String> call(ListView<String> list) {
                                    return new ColorRectCell();
                                }
                            }
        );
        // set cell selection event
        methodsList.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                        System.out.println(1);
                    }
                });
        ObservableList<String> items = FXCollections.observableArrayList (
                "Single", "Double", "Suite", "Family App");
        methodsList.setItems(items);
    }

    /**
     * set scene to be main.fxml
     * @param event     mouse click on menu item to go to Home page
     * @throws Exception
     */
    @FXML
    void gotoHomeMenuClicked(ActionEvent event) throws Exception {
        loadScene("main.fxml");
    }


    /**
     * switch current window scene to specified
     * @param scene
     * @throws Exception
     */
    private void loadScene(String scene) throws Exception {
        Stage primaryStage = (Stage) stage.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(scene));
        primaryStage.setScene(new Scene(root, 640, 400));
    }

    /**
     * cell look for ListView
     */
    static class ColorRectCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            Rectangle r = new Rectangle(0, 35);
            r.setOpacity(0);

            setGraphic(r);
            setText(item);
        }
    }
}
