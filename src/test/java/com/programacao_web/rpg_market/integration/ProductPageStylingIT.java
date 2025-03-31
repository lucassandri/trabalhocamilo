package com.programacao_web.rpg_market.integration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductPageStylingIT {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        // Setup headless chrome driver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testProductDetailPageStyling() {
        // Simulate login
        driver.get("http://localhost:" + port + "/login");
        // Assume the login form contains inputs with names "username" and "password"
        WebElement usernameInput = driver.findElement(By.name("username"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        usernameInput.sendKeys("testuser");
        passwordInput.sendKeys("password");
        // Submit the form (assume submit button type submit)
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Navigate to product details page (assume a product with id "1" exists)
        driver.get("http://localhost:" + port + "/item/1");
     
        // Verify that the product.css file is loaded by checking for a link element with href containing "css/product.css"
        List<WebElement> cssLinks = driver.findElements(By.cssSelector("link[href*='css/product.css']"));
        assertFalse(cssLinks.isEmpty(), "Product CSS file should be loaded on the product details page.");

        // Verify that a key element from the page has the expected style.
        // For example, check the "product-category-badge" element for a non-empty background color.
        WebElement badge = driver.findElement(By.cssSelector(".product-category-badge"));
        String bgColor = badge.getCssValue("background-color");
        assertNotNull(bgColor, "The badge element should have a background color defined.");
        assertFalse(bgColor.trim().isEmpty(), "The badge element should have a non-empty background color.");
    }
}
