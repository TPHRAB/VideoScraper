package controller;

import download.DownloadManager;
import org.dom4j.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import extensions.dom4j.Dom4jUtil;
import progressbar.ProgressBar;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;


public class AutoDownload {

    public static final String DIRECTORY_SEPERATOR = System.getProperty("os.name").
    		toLowerCase().contains("win") ? "\\" : "/";

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {

        // configure the SSLContext with a TrustManager
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);

        org.dom4j.Document xml = Dom4jUtil.getDocument("websites.xml");

        // get url and output path
        Scanner console = new Scanner(System.in);
        System.out.print("Please input the video url: ");
        String url = console.nextLine();
        System.out.print("Pleae input the path for generating video(s): ");
        String outputDirectory = console.nextLine();
        if (outputDirectory.trim().isEmpty()) {
        	String xpath = "/config/defaultPath[@seperator='" + DIRECTORY_SEPERATOR + "']";
            System.out.println(xpath);
        	outputDirectory = xml.selectSingleNode(xpath).getText();
        }
        File out = new File(outputDirectory);

        String xpath = "/config/methods/host[@id='" + new URL(url).getHost() + "']";
        Element host = (Element) xml.selectSingleNode(xpath);

        if (host.attributeValue("method").equals("directMP4")) {
        	directMP4(url, out, host);
        } else if (host.attributeValue("method").equals("seleniumMP4")) {
        	// be ready for ssl
    		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
    		System.setProperty("webdriver.gecko.driver", "./geckodriver.exe");

    		WebDriver browser = new FirefoxDriver();
    		// set time interval to wait after loading a new page so that xpath could work fine
    		browser.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
    		browser.get(url);
    		// get video file
    		String link = null;
    		List<WebElement> target = browser.findElements(By.xpath("//iframe"));
    		for (WebElement e : target) {
    			if (e.getAttribute("src").contains("?videoid=")) {
    				browser.switchTo().frame(e);
    				WebElement realTarget = browser.findElement(By.tagName("source"));
    				link = realTarget.getAttribute("src");
    				break;
    			}
    		}
    		browser.close();
    		File result = new File(out.getAbsolutePath() + DIRECTORY_SEPERATOR + DownloadManager.
    				getURLTitle(url) + ".mp4");
    		ProgressBar pb = new ProgressBar("Download", 11, "Thread", 1, "10");
    		pb.start();
    		DownloadManager.doDownloadSingleFile(new URL(link), result, 10, pb.getPipedWriter(),
    				new HashMap<String, String>());
    		pb.join();
        } else {
        	System.out.println("Not supported website!");
        }

        // write history
        boolean recordHistory = ((Element) xml.selectSingleNode("/config/history"))
                .attributeValue("record")
                .equals("true");
        if (recordHistory) {
            String path = ((Element) xml.selectSingleNode("/config/history")).
            		attributeValue("path");
            File history = new File(path);
            if (!history.exists()) {
                history.createNewFile();
            }
            RandomAccessFile r = new RandomAccessFile(path, "rwd");
            r.seek(history.length());
            r.writeChars(new Date() + "    " + url + "    " + DownloadManager.
            		getURLTitle(url) + "\r\n");
            r.close();
        }
        console.close();

        for (File f : new File(".").listFiles()) {
        	String regex = ".*\\.m3u8";
        	if (f.getName().matches(regex)) {
        		f.delete();
        	}
        }

        System.out.println("Finish...");
    }

	/**
	 * Get a list of links after combining links in m3u8 file with the domain name. The list starts at line of link that
	 * equals to "from"
	 * @param firstPart - the first part of the url before links in the m3u8 file
	 * @param m3u8 - m3u8 file
	 * @param from - from which line
	 * @return a list of links of video segments
	 * @throws Exception
	 */
	public static List<String> getAppendedList(String firstPart, File m3u8, String from)
			throws Exception {
		if (m3u8.getName().length() > 255) {
			m3u8 = new File(m3u8.getName().substring(0, 255));
		}
		Scanner read = new Scanner(m3u8);
		List<String> list = new ArrayList<String>();
		boolean startCombine = (from == null); // combination process starts at the first line of the m3u8 file

		while (read.hasNextLine()) {
			String line = read.nextLine();
			if (line.equals(from)) { // starts the combination process
				startCombine = true;
			}
			if (startCombine && line.charAt(0) != '#') {
				if (line.indexOf("http") == -1) {
					list.add(firstPart + line);
				} else {
					list.add(line);
				}
			}


		}
		read.close();
		return list;
	}

    public static void sololyM3u8(File dir, List<String> list) throws Exception {
    	int threads = 10;
        if (list.size() / threads == 0) {
            throw new IllegalArgumentException("list.size() / threads == 0!");
        }
        DownloadManager.downloadTSFileList(list, dir, 10, new HashMap<String, String>());
    }

    public static void directMP4(String url, File out, Element host) throws Exception {
    	if (DIRECTORY_SEPERATOR.equals("/")) {
    		System.setProperty("webdriver.gecko.driver", "./geckodriver_linux");
    	} else {
    		System.setProperty("webdriver.gecko.driver", "./geckodriver.exe");
    	}

    	WebDriver browser = new FirefoxDriver();
    	browser.manage().timeouts().implicitlyWait(15,TimeUnit.SECONDS);
    	browser.get(url);
    	String link = browser.findElements(By.tagName("video")).get(0).findElement(
    			By.tagName("source")).getAttribute("src");
    	String fileName = browser.getTitle().replaceAll("[\\\\/:*?\"<>|]]*", "");
        File result = new File(out.getAbsolutePath() + DIRECTORY_SEPERATOR + fileName + ".mp4");
        browser.close();
        int threads = 10;
        long fileLength = DownloadManager.getURLFileLength(new URL(link), null);
        if (fileLength % 10 > 0) threads++;
        ProgressBar pb = new ProgressBar("Download single file", threads, "Size", 1,
        		String.valueOf(fileLength));
        pb.start();
        DownloadManager.doDownloadSingleFile(new URL(link), result, 10, pb.getPipedWriter(), null);
        pb.join();
    }

    public static boolean videoConvert(String path) throws Exception {
            List<String> command = new ArrayList<>();
            boolean execution = false;
            if (DIRECTORY_SEPERATOR.equals("\\")) {
            	command.add("cmd.exe");
            	command.add("/c");
            	command.add("start");
            	command.add("./ffmpeg.exe");
            	execution = true;
            } else if (DIRECTORY_SEPERATOR.equals("/")) {
            	command.add("./ffmpeg");
            	execution = true;
            } else {
            	System.out.println("Not supported operating system! Please do the convertion"
            			+ " by yourself!");
            }

            // set input file and output file
            command.add("-i");
            command.add(path);
            command.add(path.substring(0, path.length() - 2) + "mp4");

            // run script
            if (execution) {
            	videoConvert(command);
            	return true;
            } else {
            	return false;
            }
    }

	public static void videoConvert(List<String> command) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();
		InputStream errorStream = process.getErrorStream();
		InputStreamReader isr = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		if (br != null) {
			br.close();
		}
		if (isr != null) {
			isr.close();
		}
		if (errorStream != null) {
			errorStream.close();
		}
	}

	public static void videoCombine(Scanner console, String url, File dir, Element host)
			throws Exception {
        System.out.print("Please input the path for combination: ");
        String path = console.nextLine();
        path = path + DIRECTORY_SEPERATOR + DownloadManager.getURLTitle(url) + ".ts";
        File result = new File(path);
        copy.TestThread.waitThreads(
                copy.TestThread.doCombine(dir.listFiles(), result));

        if (dir.getName().equals("raw")) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
            dir.delete();
        }

        String convert = host.attributeValue("autoConvert");
        if (convert != null && convert.equals("true")) {
            if (videoConvert(path)) result.delete();
        }
    }
}

