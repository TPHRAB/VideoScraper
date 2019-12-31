package download2;

import javafx.scene.layout.AnchorPane;
import org.dom4j.Document;

public abstract class DownloadModule {
    public abstract void loadScene(AnchorPane informationPanel);
    public Document loadSetting() {
        return null;
    }
}
