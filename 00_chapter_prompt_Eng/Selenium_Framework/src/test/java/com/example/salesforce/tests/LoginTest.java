package com.example.salesforce.tests;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

import com.example.salesforce.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class LoginTest {
    private static final String LOGIN_URL = "https://login.salesforce.com/?locale=in";
    private static final String INVALID_USERNAME = "invalid.user@example.invalid";
    private static final String INVALID_PASSWORD = "NotAValidPassword-9281";

    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeTest
    public void startBrowser() {
        String browser = System.getProperty("browser", "chrome").toLowerCase(Locale.ROOT);
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        try {
            driver = createDriver(browser, headless);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        } catch (IllegalArgumentException | WebDriverException exception) {
            throw new IllegalStateException("Unable to initialize the " + browser + " browser", exception);
        }
    }

    @BeforeMethod
    public void openLoginPage() {
        loginPage = new LoginPage(Objects.requireNonNull(driver, "WebDriver was not initialized"));
        loginPage.open(LOGIN_URL);
    }

    @Test(description = "Valid Salesforce credentials open the authenticated application")
    public void shouldAuthenticateWithValidCredentials() {
        String username = requiredEnvironmentVariable("SF_USERNAME");
        String password = requiredEnvironmentVariable("SF_PASSWORD");

        loginPage.login(username, password);

        Assert.assertTrue(loginPage.isAuthenticated(),
                "Salesforce did not reach the authenticated application; verify credentials and MFA requirements");
    }

    @Test(description = "Invalid Salesforce credentials show an error and Remember Me remains operable")
    public void shouldRejectInvalidCredentials() {
        boolean initialRememberMeState = loginPage.isRememberMeSelected();
        loginPage.setRememberMeSelected(!initialRememberMeState);
        Assert.assertEquals(loginPage.isRememberMeSelected(), !initialRememberMeState,
                "Remember Me did not change to the requested state");
        loginPage.setRememberMeSelected(initialRememberMeState);

        loginPage.login(INVALID_USERNAME, INVALID_PASSWORD);

        String errorMessage = loginPage.getLoginErrorMessage();
        Assert.assertFalse(errorMessage.isBlank(), "Invalid credentials should display a login error message");
    }

    @AfterTest(alwaysRun = true)
    public void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    private WebDriver createDriver(String browser, boolean headless) {
        return switch (browser) {
            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                yield new ChromeDriver(options);
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                yield new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                yield new EdgeDriver(options);
            }
            default -> throw new IllegalArgumentException("Supported browsers are chrome, firefox, and edge");
        };
    }

    private String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + name + " environment variable to run the valid login test");
        }
        return value;
    }
}