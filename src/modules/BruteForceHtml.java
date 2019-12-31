package modules;

import download2.DownloadModule;
import extensions.download.DownloadManager;
import javafx.scene.layout.AnchorPane;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteForceHtml extends DownloadModule {
    private DownloadManager manager;

    @Override
    public void loadScene(AnchorPane informationPanel) {

    }

    public boolean startDownload(String url) {
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.get(url);
        Matcher matcher = Pattern.compile("\"https?://[^\"]*?mp4.*?\"").matcher(driver.getPageSource());
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
        // close browser
        driver.close();
        return true;
    }
}