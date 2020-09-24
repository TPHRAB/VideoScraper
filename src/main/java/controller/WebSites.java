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
    WebDriver driver;
    WebDriverWait wait;
    String outputDirectory;

    public WebSites(String outputDirectory) {
        // initialize headless browser
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        this.outputDirectory = outputDirectory;
    }

    private WebElement waitElement(By selector) {
        return wait.until(driver -> driver.findElement(selector));
    }

    public void youtube(String link) {
        driver.get(YOUTUBE_SOURCE);
        driver.findElement(By.id("txt-url")).sendKeys(link); // fill in video link
        driver.findElement(By.id("btn-submit")).click(); // generate download link
        waitElement(By.className("btn-success")).click(); // choose the best resolution

        // link of the video to be downloaded
        String videoLink = waitElement(By.cssSelector(".has-success .btn-success")).getAttribute("href");

        // name of the file to be generated
        String outputFile = waitElement(By.id("exampleModalLabel")).getText();
        outputFile = outputFile.replaceAll("/", "\\\\"); // replace forbidden characters in naming
        outputFile += ".mp4";

        try {
            download(new URL(videoLink), Paths.get(outputDirectory, outputFile).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Download the file from link
     * @param video - video file to download
     * @param outputPath - path of file to write in
     */
    private void download(URL video, String outputPath) throws IOException  {
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
            while (bytesTransferred < fileSize) { // when the inputChannel is not null
                bytesTransferred += outputChannel.transferFrom(inputChannel, bytesTransferred, 1024 * 1024); // read 1 Mb at a time
                pb.stepTo(bytesTransferred);
            }
        }
    }

    public void close() {
        driver.close();
    }
}
