package controller;

// selenium
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

// utils
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Containing methods to download videos from different websites
 */
public class WebSites {
    private static String YOUTUBE_SOURCE = "https://www.y2mate.com/en60";
    private static String BILIBILI_SOURCE = "https://keepv.id/download-bilibili-videos";
    private static String TWITTER_SOURCE = "https://keepv.id/download-twitter-videos";
    WebDriver driver;
    WebDriverWait wait;
    String link; // video link on video hosting websties
    String outputDirectory;

    public WebSites(String link, String outputDirectory) {
        // initialize headless browser
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless", "");
        // disable log
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        this.link = link;
        this.outputDirectory = outputDirectory;
    }

    private WebElement waitElement(By selector) {
        return wait.until(driver -> driver.findElement(selector));
    }

    /**
     * Download youtube videos
     * @throws Exception when download fails
     */
    public void youtube() throws Exception {
        driver.get(YOUTUBE_SOURCE);
        driver.findElement(By.id("txt-url")).sendKeys(link); // fill in video link
        driver.findElement(By.id("btn-submit")).click(); // generate download link
        waitElement(By.className("btn-success")).click(); // choose the best resolution

        // link of the video to be downloaded
        String videoLink = waitElement(By.cssSelector(".has-success .btn-success")).getAttribute("href");

        // name of the file to be generated
        String outputFile = waitElement(By.id("exampleModalLabel")).getText();
        outputFile = replaceForbiddenChars(outputFile); // replace forbidden characters in naming
        outputFile += ".mp4";

        try {
            download(new URL(videoLink), outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Download failed");
        }
    }

    /**
     * Download videos from keevid
     * @param format - format of the video to download
     * @throws Exception when download fails
     */
    private void keepvid(String format) throws Exception {
        driver.findElement(By.id("dlURL")).sendKeys(link); // fill in video link
        driver.findElement(By.id("dlBTN1")).click(); // click go
        driver.findElement(By.id("dlBTN1")).click(); // click go again
        String videoLink = waitElement(By.className("vdlbtn")).getAttribute("href");
        String outputFile = driver.findElement(By.cssSelector("h2.mb-3")).getText();
        outputFile = replaceForbiddenChars(outputFile) + '.' + format;
        try {
            download(new URL(videoLink), outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Download failed");
        }
    }

    /**
     * Download bilibili videos
     * @throws Exception when download fails
     */
    public void bilibili() throws Exception {
        driver.get(BILIBILI_SOURCE);
        keepvid("flv");
    }

    public void twitter() throws Exception {
        driver.get(TWITTER_SOURCE);
        keepvid("mp4");
    }

    /**
     * Replace forbidden characters in naming files
     * @return A valid file name
     */
    private String replaceForbiddenChars(String name) {
        return name.replaceAll("/", "\\\\");
    }

    /**
     * Download the file from link
     * @param video - video file to download
     * @param outputFile - file's name to generate
     */
    private void download(URL video, String outputFile) throws IOException  {
        String outputPath = Paths.get(outputDirectory, outputFile).toString();

        // get total bytes of the file to download
        URLConnection connection = video.openConnection();
        long fileSize = connection.getHeaderFieldLong("Content-Length", 0);

        // open stream
        ReadableByteChannel inputChannel = Channels.newChannel(video.openStream());
        FileChannel outputChannel = new FileOutputStream(outputPath).getChannel();
        long bytesTransferred = 0;

        // start progress bar
        System.out.println("----------------------------");
        System.out.printf("File: %s\n", outputPath);
        try (ProgressBar pb = new ProgressBarBuilder()
                                    .setInitialMax(fileSize)
                                    .setTaskName("Downloading")
                                    .setStyle(ProgressBarStyle.ASCII)
                                    .build()) { // initialize progress bar
            long initialZeroByteTime = -1;
            while (bytesTransferred < fileSize) { // when the inputChannel is not null
                long bytesRead = outputChannel.transferFrom(inputChannel, bytesTransferred, 1024 * 1024);
                bytesTransferred +=  bytesRead; // read 1 Mb at a time
                pb.stepTo(bytesTransferred);

                if (bytesRead == 0) {
                    long current = System.currentTimeMillis();
                    if (initialZeroByteTime == -1) {
                        initialZeroByteTime = current;
                    } else if ((current - initialZeroByteTime) / 1000 > 5) { // after waiting 5 seconds, assume file download successful
                        System.out.println("Maximum time reached");
                        break;
                    }
                } else { // reset
                    initialZeroByteTime = -1;
                }
            }
        }
    }

    /**
     * Close the browser
     */
    public void close() {
        driver.quit();
    }
}
