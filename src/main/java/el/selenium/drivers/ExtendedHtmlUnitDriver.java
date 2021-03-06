package el.selenium.drivers;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import el.selenium.utils.CommandExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.internal.Base64Encoder;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExtendedHtmlUnitDriver extends HtmlUnitDriver implements TakesScreenshot {

    private final static Logger log = LoggerFactory.getLogger(ExtendedHtmlUnitDriver.class);

    private static Map<String, byte[]> imagesCache = Collections.synchronizedMap(new HashMap<>());

    private static Map<String, String> cssjsCache = Collections.synchronizedMap(new HashMap<>());

    // http://stackoverflow.com/questions/4652777/java-regex-to-get-the-urls-from-css
    private final static Pattern cssUrlPattern = Pattern.compile("background(-image)?[\\s]*:[^url]*url[\\s]*\\([\\s]*([^\\)]*)[\\s]*\\)[\\s]*");// ?<url>

    public ExtendedHtmlUnitDriver() {
        super();
    }

    public ExtendedHtmlUnitDriver(boolean enableJavascript) {
        super(enableJavascript);
    }

    public ExtendedHtmlUnitDriver(Capabilities capabilities) {
        super(capabilities);
    }

    public ExtendedHtmlUnitDriver(BrowserVersion version) {
        super(version);
        DesiredCapabilities var = ((DesiredCapabilities) getCapabilities());
        var.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
    }

    @Override
    protected WebClient modifyWebClient(WebClient client) {
        client.setRefreshHandler((page, url, seconds) -> {
        });
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        byte[] archive = new byte[0];
        try {
            archive = downloadCssAndImages(getWebClient(), (HtmlPage) getCurrentWindow().getEnclosedPage());
        } catch (Exception e) {
        }
        if (target.equals(OutputType.BASE64)) {
            return target.convertFromBase64Png(new Base64Encoder().encode(archive));
        }
        if (target.equals(OutputType.BYTES)) {
            return (X) archive;
        }
        return (X) archive;
    }

    // http://stackoverflow.com/questions/2244272/how-can-i-tell-htmlunits-webclient-to-download-images-and-css
    protected byte[] downloadCssAndImages(WebClient webClient, HtmlPage page) throws Exception {
        WebWindow currentWindow = webClient.getCurrentWindow();
        Map<String, String> urlMapping = new HashMap<String, String>();
        Map<String, byte[]> files = new HashMap<>();
        WebWindow window;
        try {
            window = webClient.getWebWindowByName(page.getUrl().toString() + "_screenshot");
            webClient.getPage(window, new WebRequest(page.getUrl()));
        } catch (Exception e) {
            window = webClient.openWindow(page.getUrl(), page.getUrl().toString() + "_screenshot");
        }

        String xPathExpression = "//*[name() = 'img' or name() = 'link' and (@type = 'text/css' or @type = 'image/x-icon') or  @type = 'text/javascript']";
        List<?> resultList = page.getByXPath(xPathExpression);

        for (Object aResultList : resultList) {
            try {
                HtmlElement el = (HtmlElement) aResultList;
                String resourceSourcePath = el.getAttribute("src").equals("") ? el.getAttribute("href") : el
                        .getAttribute("src");
                if (resourceSourcePath == null || resourceSourcePath.equals(""))
                    continue;
                URL resourceRemoteLink = page.getFullyQualifiedUrl(resourceSourcePath);
                String resourceLocalPath = mapLocalUrl(page, resourceRemoteLink, resourceSourcePath, urlMapping);
                urlMapping.put(resourceSourcePath, resourceLocalPath);
                if (!resourceRemoteLink.toString().endsWith(".css")) {
                    byte[] image = downloadImage(webClient, window, resourceRemoteLink);
                    files.put(resourceLocalPath, image);
                } else {
                    String css = downloadCss(webClient, window, resourceRemoteLink);
                    for (String cssImagePath : getLinksFromCss(css)) {
                        URL cssImagelink = page.getFullyQualifiedUrl(cssImagePath.replace("\"", "").replace("\'", "")
                                .replace(" ", ""));
                        String cssImageLocalPath = mapLocalUrl(page, cssImagelink, cssImagePath, urlMapping);
                        files.put(cssImageLocalPath, downloadImage(webClient, window, cssImagelink));
                    }
                    files.put(resourceLocalPath, replaceRemoteUrlsWithLocal(css, urlMapping)
                            .replace("resources/", "./").getBytes());
                }
            } catch (Exception e) {
            }
        }
        String pagesrc = replaceRemoteUrlsWithLocal(page.getWebResponse().getContentAsString(), urlMapping);
        files.put("page.html", pagesrc.getBytes());
        webClient.setCurrentWindow(currentWindow);
        return createZip(files);
    }

    private String downloadCss(WebClient webClient, WebWindow window, URL resourceUrl) throws Exception {
        if (cssjsCache.get(resourceUrl.toString()) == null) {
            cssjsCache.put(resourceUrl.toString(), webClient.getPage(window, new WebRequest(resourceUrl))
                    .getWebResponse().getContentAsString());

        }
        return cssjsCache.get(resourceUrl.toString());
    }

    private byte[] downloadImage(WebClient webClient, WebWindow window, URL resourceUrl) throws Exception {
        if (imagesCache.get(resourceUrl.toString()) == null) {
            imagesCache.put(
                    resourceUrl.toString(),
                    IOUtils.toByteArray(webClient.getPage(window, new WebRequest(resourceUrl)).getWebResponse()
                            .getContentAsStream()));
        }
        return imagesCache.get(resourceUrl.toString());
    }

    public static byte[] createZip(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zipfile = new ZipOutputStream(bos);
        Iterator<String> i = files.keySet().iterator();
        String fileName;
        ZipEntry zipentry;
        while (i.hasNext()) {
            fileName = i.next();
            zipentry = new ZipEntry(fileName);
            zipfile.putNextEntry(zipentry);
            zipfile.write(files.get(fileName));
        }
        zipfile.close();
        return bos.toByteArray();
    }

    private List<String> getLinksFromCss(String css) {
        List<String> result = new LinkedList<String>();
        Matcher m = cssUrlPattern.matcher(css);
        while (m.find()) { // find next match
            result.add(m.group(2));
        }
        return result;
    }

    private String replaceRemoteUrlsWithLocal(String source, Map<String, String> replacement) {
        for (String object : replacement.keySet()) {
            // background:url(http://org.com/images/image.gif)
            source = source.replace(object, replacement.get(object));
        }
        return source;
    }

    private String mapLocalUrl(HtmlPage page, URL link, String path, Map<String, String> replacementToAdd) throws Exception {
        String resultingFileName = "resources/" + FilenameUtils.getName(link.getFile());
        replacementToAdd.put(path, resultingFileName);
        return resultingFileName;
    }

    @Override
    public void quit() {
        CommandExecutor.executeOnIsolation(super::quit);
    }
}
