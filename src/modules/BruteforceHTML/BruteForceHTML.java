package modules.BruteforceHTML;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteForceHTML {
    private String[] formats = {"MP4", "VOB", "M4S", "m3u8"};

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
        getFormatRegex();
        String url = urlTextField.getText().trim();
        String downloadPath = downloadPathTextField.getText().trim();


        System.setProperty("webdriver.gecko.driver", "./geckodriver.exe");
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.get(url);
        Matcher matcher = Pattern.compile(getFormatRegex()).matcher(driver.getPageSource());
        Set<String> videoURLs = new HashSet<>(); // store all unique video urls
        while (matcher.find()) {
            videoURLs.add(matcher.group());
        }
        // close browser
        driver.close();

    }

    private String getFormatRegex() {
        String regex = "(";
        for (int i = 0; i < formats.length - 1; i++) {
            regex += formats[i] + "|";
        }
        regex += formats[formats.length - 1] + ")";
        return "\"https?://[^\"=]*?" + regex + ".*?\"";
    }

}