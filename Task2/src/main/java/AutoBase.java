import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AutoBase {

    public Map<String, String> getUrlFilters(String filterUrl) throws MalformedURLException {
        URL url = new URL(filterUrl);
        String s = url.getQuery();
        Map<String, String> filterMap = new HashMap<>();
        for (String pair : s.split("&")) {
            filterMap.put(pair.split("=")[0], pair.split("=")[1]);
        }
        return filterMap;
    }

    public Map<String, String> getUIFilters(WebDriver driver) {
        WebElement listContainerElement = driver.findElement(By.xpath("//div[@class='listContainer___3wEKn']"));
        WebElement unorderedListElement = listContainerElement.findElement(By.xpath("./child::*"));
        List<WebElement> itemElements = unorderedListElement.findElements(By.xpath("./child::*"));

        Map<String, String> filterMap = new HashMap<>();
        for (WebElement itemElement : itemElements) {
            String item = itemElement.getText();
            if (!item.isEmpty()) {
                String[] pair = item.split(": ");
                String key = pair[0];

                String value = pair[1];
                filterMap.put(key, value);
            }
        }
        return filterMap;
    }

    public List<WebElement> getProductWebelementList(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        List<WebElement> products = driver.findElements(By.xpath("//*[@class='ReactVirtualized__Grid__innerScrollContainer']/child::*"));
        return products;
    }

    public Set<Product> getProductSet(WebDriver driver) {
        List<Product> productDetailList = new ArrayList<Product>();
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        WebElement lastProductBeforeScroll = null;
        boolean found = false;

        while (true) {
            List<WebElement> productList = this.getProductWebelementList(driver);
            WebElement lastProductAfterScroll = productList.get(productList.size() - 1);
            if (lastProductBeforeScroll != null && lastProductBeforeScroll.getText().equals(lastProductAfterScroll.getText())) {
                break;
            } else {
                lastProductBeforeScroll = lastProductAfterScroll;
            }
            for (WebElement product : productList) {
                try {
                    WebElement titleElement = product.findElement(By.className("adTitle___1MVoL"));
                    String title = titleElement.getText();
                    int mileage = 0;

                    WebElement specWebElement = product.findElement(By.className("specList___2i0rY"));
                    List<WebElement> specList = product.findElements(By.className("specItem___2gMHn"));
                    for (WebElement spec : specList) {
                        String specString = spec.getText();
                        if (specString.indexOf("km") != -1) {
                            String mileageStr = specString.replaceAll("[^0-9]", "");
                            mileage = Integer.parseInt(mileageStr);
                        }
                    }
                    Product myProduct = new Product(title, mileage);
                    productDetailList.add(myProduct);
                } catch (NoSuchElementException e) {
                }
            }
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            jse.executeScript("arguments[0].scrollIntoView(true)", lastProductAfterScroll);

            /*  I tried the below logic (visibility fo footer)  to end the scroll but footer  was visible in elements  before scrolling but not in UI
            if (found){
                break;
            }
            found = this.isElementVisible(driver, driver.findElement(By.tagName("footer")), 1);

             */
        }
        Set<Product> productSet = new HashSet<Product>(productDetailList);

        System.out.println("Number of Products = " + productSet.size());
        return productSet;
    }
}