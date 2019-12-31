package download2;

import extensions.download.DownloadManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteForceHtml implements DownloadModule {
    private DownloadManager manager;

    @Override
    public boolean startDownload() {
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.get("http://www.imomoe.io/player/7584-0-0.html");
        Matcher matcher = Pattern.compile("\"https?://[^\"]*?mp4.*?\"").matcher(driver.getPageSource());
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
        // close browser
        driver.close();
        return true;
    }
}
