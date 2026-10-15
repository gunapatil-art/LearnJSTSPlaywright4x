package com.example.salesforce.pages;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;

public class LoginPage {
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(20);

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//input[@id='username']")
    private WebElement usernameField;

    @FindBy(xpath = "//input[@id='password']")
    private WebElement passwordField;

    @FindBy(xpath = "//input[@id='Login']")
    private WebElement loginButton;

    @FindBy(xpath = "//input[@id='rememberUn']")
    private WebElement rememberMeCheckbox;

    @FindBy(xpath = "//div[@id='error' or contains(@class, 'loginError')]")
    private WebElement loginError;

    @FindBy(xpath = "//button[@title='App Launcher'] | //button[@aria-label='App Launcher'] | //div[contains(@class, 'oneAppNavContainer')]")
    private List<WebElement> authenticatedAppIndicators;

    public LoginPage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver must not be null");
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        PageFactory.initElements(driver, this);
    }

    public void open(String url) {
        execute("open the Salesforce login page", () -> {
            driver.get(url);
            wait.until(ExpectedConditions.visibilityOf(usernameField));
            return null;
        });
    }

    public void login(String username, String password) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        execute("submit the login form", () -> {
            wait.until(ExpectedConditions.visibilityOf(usernameField));
            usernameField.clear();
            usernameField.sendKeys(username);
            passwordField.clear();
            passwordField.sendKeys(password);
            wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
            return null;
        });
    }

    public boolean isRememberMeSelected() {
        return execute("read the Remember Me selection", () ->
                wait.until(ExpectedConditions.visibilityOf(rememberMeCheckbox)).isSelected());
    }

    public void setRememberMeSelected(boolean selected) {
        execute("update the Remember Me selection", () -> {
            wait.until(ExpectedConditions.elementToBeClickable(rememberMeCheckbox));
            if (rememberMeCheckbox.isSelected() != selected) {
                rememberMeCheckbox.click();
            }
            return null;
        });
    }

    public String getLoginErrorMessage() {
        return execute("read the login error message", () ->
                wait.until(ExpectedConditions.visibilityOf(loginError)).getText().trim());
    }

    public boolean isAuthenticated() {
        try {
            return wait.until(ignored -> authenticatedAppIndicators.stream()
                    .anyMatch(WebElement::isDisplayed));
        } catch (TimeoutException exception) {
            return false;
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Unable to verify the authenticated Salesforce page", exception);
        }
    }

    private <T> T execute(String action, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Unable to " + action, exception);
        }
    }
}