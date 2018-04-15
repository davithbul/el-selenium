package el.selenium.drivers.page.pool;

import el.selenium.factory.WebDriverFactory;
import el.selenium.model.ProfileStrategy;
import el.selenium.drivers.page.PageDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * PageDriverPool contains pool of PageDrivers.
 * The PageDriverPool is thread safe.
 */
public class PageDriverPool {

    private final static Logger log = LoggerFactory.getLogger(PageDriverPool.class);

    /**
     * This variable let's the same thread to get the same page wrapper
     **/
    private ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    /**
     * This pooling contains all the page wrappers
     */
    private volatile List<PageDriver> pageDriverPool = Collections.synchronizedList(new ArrayList<>());

    private volatile Map<String, PageDriver> busyPageWrappers = new HashMap<>();

    /**
     * Those are extended page wrappers, which created in case all page wrappers from pageDriverPool are busy
     */
    private volatile Map<String, PageDriver> extendedPageWrappers = new HashMap<>();

    /**
     * Reference should be the id of pageWrapper
     */
    private volatile Map<String, AtomicInteger> referenceCounter = new HashMap<>();

    private volatile int tryingLimit = 10;
    private volatile int maxPoolSize = 30;
    private volatile boolean expandable = true;
    private volatile boolean shareReference = true;
    private volatile boolean cleanAfterUsage = false;

    private volatile WebDriverFactory.Browser browser;
    private volatile ProfileStrategy profileStrategy;

    public PageDriverPool(WebDriverFactory.Browser browser, ProfileStrategy profileStrategy, int poolSize) {
        this.browser = browser;
        this.profileStrategy = profileStrategy;

        IntStream.range(0, poolSize)
                .parallel()
                .forEach(i -> {
                    String reference = getReference();
                    PageDriver pageDriver = new PageDriver(reference, browser, profileStrategy);
                    pageDriverPool.add(pageDriver);
                });
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setShareReference(boolean shareReference) {
        this.shareReference = shareReference;
    }

    public void setCleanAfterUsage(boolean cleanAfterUsage) {
        this.cleanAfterUsage = cleanAfterUsage;
    }

    public PageDriver take() {
        String reference = getReference();

        //at first checking if there is already ready pageWrapper
        synchronized (this) {
            PageDriver pageDriver = null;
            if (referenceCounter.get(reference) != null) {
                pageDriver = busyPageWrappers.get(reference);
                if (pageDriver == null) {
                    pageDriver = extendedPageWrappers.get(reference);
                }
                referenceCounter.get(reference).incrementAndGet();
            } else if (!pageDriverPool.isEmpty()) {
                pageDriver = pageDriverPool.remove(0);
                registerPageWrapper(pageDriver.getId(), pageDriver, false);
            }
            if (pageDriver != null) {
                return pageDriver;
            }
        }

        //no initialized pageWrapper is available, initializing new one
        boolean initPageWrapper = false;
        synchronized (this) {
            if (busyPageWrappers.size() + extendedPageWrappers.size() < maxPoolSize) {
                initPageWrapper = true;
                if (!expandable) {
                    extendedPageWrappers.put(reference, null);
                } else {
                    busyPageWrappers.put(reference, null);
                }
            }
        }

        if (!initPageWrapper) {
            try {
                log.debug("No PageWrapper is available, sleeping 40 seconds");
                TimeUnit.SECONDS.sleep(40);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            return take();
        } else {
            //now initializing new pageWrapper
            PageDriver pageDriver = null;
            while (tryingLimit > 0) {
                try {
                    pageDriver = new PageDriver(reference, browser, profileStrategy);
                    break;
                } catch (WebDriverException e) {
                    tryingLimit--;
                    if (tryingLimit <= 0) {
                        throw new RuntimeException("No PageWrapper is available and trying limit is exceeded");
                    }
                }
            }
            registerPageWrapper(reference, pageDriver, !expandable);
            return pageDriver;
        }
    }

    private synchronized void registerPageWrapper(String reference, PageDriver pageDriver, boolean isExtended) {
        //should be added to extension
        if (isExtended) {
            extendedPageWrappers.put(reference, pageDriver);
        } else {
            busyPageWrappers.put(reference, pageDriver);
        }

        //add in reference counter
        if (referenceCounter.get(reference) != null) {
            referenceCounter.get(reference).incrementAndGet();
        } else {
            referenceCounter.put(reference, new AtomicInteger(1));
        }
    }

    public synchronized void free(PageDriver pageDriver) {
        if (pageDriver == null) {
            return;
        }

        if (cleanAfterUsage) {
            try {
                pageDriver.cleanFullStorage();
            } catch (WebDriverException e) {
                if (!e.getMessage().contains("is disabled")) {
                    log.error(e.getMessage(), e);
                    pageDriver.restartBrowser();
                }
            }
        }

        String reference = pageDriver.getId();
        int referenceCount = referenceCounter.get(reference).decrementAndGet();
        if (referenceCount <= 0) {
            referenceCounter.remove(reference);
            PageDriver remove = busyPageWrappers.remove(reference);
            if (remove != null) {
                pageDriverPool.add(pageDriver);
            } else {
                extendedPageWrappers.remove(reference);
                pageDriver.close();
            }
        }
    }

    private String getReference() {
        if (shareReference) {
            return threadLocal.get();
        } else {
            return UUID.randomUUID().toString();
        }
    }

    public void destroy() {
        referenceCounter.clear();
        pageDriverPool.forEach(PageDriver::close);
        busyPageWrappers.values().forEach(PageDriver::close);
        extendedPageWrappers.values().forEach(PageDriver::close);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }
}
