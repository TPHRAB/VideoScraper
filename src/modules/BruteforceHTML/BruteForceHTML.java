package modules.BruteforceHTML;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteForceHTML {

    @FXML
    private TextField urlTextField;

    @FXML
    private TextField downloadPathTextField;

    @FXML
    private Button browseButton;

    @FXML
    private Button startButton;

    @FXML
    void browseButtonClicked(MouseEvent event) {

    }

    @FXML
    void startButtonClicked(MouseEvent event) {
        String url = urlTextField.getText().trim();
        String downloadPath = downloadPathTextField.getText().trim();


        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.get(url);
        Matcher matcher = Pattern.compile("\"https?://[^\"]*?mp4.*?\"").matcher(driver.getPageSource());
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
        // close browser
        driver.close();
    }

}