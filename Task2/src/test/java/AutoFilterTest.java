import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AutoFilterTest {

    public WebDriver driver;
    AutoBase autobase = new AutoBase();
    String driverPath = (".//chromedriver.exe");
    String autoURL = "https://www.autohero.com/de/search/";


    @DataProvider(name = "searchParameters")
    public Object[][] searchParameters() {
        Object[][] searchParameters = new Object[1][3];
        searchParameters[0][0] = "Volkswagen";
        searchParameters[0][1] = "Golf";
        searchParameters[0][2] = "25.000 km";
        return searchParameters;
    }

    @BeforeSuite
    public void launchBrowser() {
        System.out.println("launching chrome browser");
        System.setProperty("webdriver.chrome.driver", driverPath);
        driver = new ChromeDriver();
        driver.get(autoURL);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().window().fullscreen();
        WebElement cookiesClass = driver.findElement(By.xpath("/html/body/div[3]/div/form/div[2]/button[2]"));
        cookiesClass.click();
    }

    @Test(priority = 0, dataProvider = "searchParameters")
    public void excecuteFilterActionTest(String brandName, String modelName, String mileageValue) {
        System.out.println("Executing filter Action Test");
        WebElement modelSelect = driver.findElement(By.id("carMakeFilter"));
        modelSelect.click();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        WebElement brand = driver.findElement(By.xpath("( //input[contains(@value,'" + brandName + "')])[1]"));
        brand.click();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        WebElement model = driver.findElement(By.xpath("( //input[contains(@value,'" + modelName + "')])"));
        model.click();
        WebElement basicFilter = driver.findElement(By.id("basicFilter"));
        basicFilter.click();
        Select mileageSelect = new Select(driver.findElement(By.id("rangeEnd")));
        mileageSelect.selectByVisibleText(mileageValue);
        basicFilter.click();
    }

    @Test(priority = 1, dataProvider = "searchParameters")
    public void validateFilterInUrlTest(String brandName, String modelName, String mileageValue) throws MalformedURLException {
        System.out.println("Validating URL after applying Filter");
        SoftAssert softAssert = new SoftAssert();
        String url = driver.getCurrentUrl();
        Map<String, String> expectedResult = new HashMap<String, String>();

        expectedResult.put("brand", brandName.toUpperCase());
        expectedResult.put("model", brandName.toUpperCase() + "." + modelName);
        expectedResult.put("mileageMax", mileageValue.replaceAll("[^0-9]", ""));

        driver.getCurrentUrl();
        Map<String, String> actualResult = autobase.getUrlFilters(url);
        softAssert.assertEquals(expectedResult, actualResult);
    }


    @Test(priority = 2, dataProvider = "searchParameters")
    public void validateFilterInUITest(String brandName, String modelName, String mileageValue) {
        System.out.println("Validating Applied Filters list  in UI ");
        SoftAssert softAssert = new SoftAssert();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        Map<String, String> actualResult = autobase.getUIFilters(driver);
        Map<String, String> expectedResult = new HashMap<String, String>();
        expectedResult.put("Marke", brandName);
        expectedResult.put("Modell", modelName);
        expectedResult.put("Kilometer", "Bis " + mileageValue);
        softAssert.assertEquals(expectedResult, actualResult);
    }


    @Test(priority = 3, dataProvider = "searchParameters")
    public void resultsValidateTest(String brandName, String modelName, String mileageValue) {
        SoftAssert softAssert = new SoftAssert();
        System.out.println("Validating the products obtained on use of filter ");
        Set<Product> productSet = autobase.getProductSet(driver);
        int mileage = Integer.parseInt(mileageValue.replaceAll("[^0-9]", ""));
        for (Product product : productSet) {
            String title = product.getName();
            boolean isBrandValid = (title.indexOf(brandName) != -1);
            boolean isModelValid = (title.indexOf(modelName) != -1);
            softAssert.assertTrue(isBrandValid);
            softAssert.assertTrue(isModelValid);
            boolean isMileageValid = (product.getMileage() <= mileage);
            softAssert.assertTrue(isMileageValid);
            softAssert.assertAll();
        }
    }

    @AfterSuite
    public void terminateBrowser() {
        driver.close();
    }
}