package download2;

import extensions.dom4j.Dom4jUtil;
import extensions.download.Download;
import org.dom4j.Document;
import org.dom4j.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.net.ssl.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoDownload {
    public static void main(String[] args) throws Exception {
        // configure the SSLContext with a TrustManager
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);

        System.setProperty("webdriver.gecko.driver", "./geckodriver_linux");

        Scanner sc = new Scanner(System.in);

        System.out.print("Please input the url: ");
        String url = sc.nextLine();

        // get xml
        Document xml = Dom4jUtil.getDocument("modules.xml");
        Element method = (Element) xml.selectSingleNode("/modules/specified/" + url.substring(0, url.lastIndexOf("/")));
        if (method != null) {
            Class moduleClass = Class.forName(method.getTextTrim());
            DownloadModule module = (DownloadModule) moduleClass.newInstance();
            module.startDownload();
        } else {
            Element unspecified = (Element) xml.selectSingleNode("/modules/unspecified");
            for (Element m : unspecified.elements()) {
                Class moduleClass = Class.forName(m.selectSingleNode("./class").getText());
                DownloadModule module = (DownloadModule) moduleClass.getConstructor().newInstance();
                if (module.startDownload()) {
                    break;
                }
            }
        }
    }


    // trust all ssl certificate
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
}
