# el-selenium
Provides safe and tolerant WebDriver crawling, handles transient errors.

Provides tools and utilities for WebDriver crawling or filling any data using automated browser. It's extension to official Selenium and it's WebDriver.
All major browsers are supported: Chrome, Firefox, HtmlUnit, PhantomJS. The central functional class is el.selenium.services.PageDriver which is a wrapper of real instance of WebDriver.

* For Chrome WebDriver you need to specify WebDriver.chrome.driver referring to chrome WebDriver executable - https://sites.google.com/a/chromium.org/chromedriver/downloads 

* For PhantomJS driver you need to specify phantomjs.binary.path

## Usage
There are 2 ways to use the PageDriver, either directly initializing or using PageDriverPool. PageDriverPool is recommended for high intense crawling applications.

