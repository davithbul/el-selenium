# el-selenium
Provides safe and tolerant webdriver crawling, handles transient errors.

Provides tools and utilities for webDriver crawling or filling any data using automated browser. It's extension to official Selenium and it's webDriver.
All major browsers are supported: Chrome, Firefox, HtmlUnity, PhantomJS
aThe central functional class is el.selenium.services.PageDriver which is a wrapper of real instance of WebDriver.

* For Chrome WebDriver you need to specify webdriver.chrome.driver referring to chrome webdriver executable - https://sites.google.com/a/chromium.org/chromedriver/downloads 

* For PhantomJS driver you need to specify phantomjs.binary.path
