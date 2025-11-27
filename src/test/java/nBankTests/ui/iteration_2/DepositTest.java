package nBankTests.ui.iteration_2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import utils.AccountData;
import utils.UserData;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;
import static generatos.RandomData.getDepositAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class DepositTest {

    private static AccountData account;
    private static UserData user;

    public final SelenideElement depositMoneyButton = $(byText("\uD83D\uDCB0 Deposit Money"));
    public final SelenideElement depositMoneyHeader = $(byText("💰 Deposit Money"));
    public final SelenideElement selectAccountFieldTitle = $(byText("Select Account:"));
    public final SelenideElement selectAccountField = $(".account-selector");
    public final SelenideElement amountFieldTitle = $(byText("Enter Amount:"));
    public final SelenideElement amountField = $(".deposit-input");
    public final SelenideElement depositButton = $(byText("\uD83D\uDCB5 Deposit"));


    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.1.251:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        account = UserSteps.createAccount(user);
        String accountNumber = account.accountNumber();
        Double depositAmount = getDepositAmount();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 1: юзер кликает на Deposit Money
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);

        //ШАГ 2: Проверяем поле для выбора счета и выбираем счет
        selectAccountFieldTitle.shouldBe(visible);
        selectAccountField.shouldBe(visible).shouldHave(text("-- Choose an account --"));
        selectAccountField.click();

        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
        selectAccount.click();
        selectAccountField.shouldHave(text(accountNumber));

        //ШАГ 3: Проверяем поле для ввода суммы и вводим сумму
        amountFieldTitle.shouldBe(visible);
        amountFieldTitle.shouldBe(visible).shouldHave(text("Enter amount"));
        amountField.sendKeys(depositAmount.toString());
        depositButton.shouldBe(visible).click();

        // ШАГ 4: проверка сообщения об успешной операции
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully deposited $" + depositAmount + " to account " + accountNumber + "!");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/dashboard");

        //ШАГ 5: проверка суммы на UI и через API
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);
        selectAccount.shouldHave(text(depositAmount.toString()));

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isEqualTo(depositAmount, within(0.0001));
    }

    @Test
    public void userCanNotDepositWithEmptyAccount() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        account = UserSteps.createAccount(user);
        String accountNumber = account.accountNumber();
        Double depositAmount = getDepositAmount();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 1: юзер кликает на Deposit Money
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);

        //ШАГ 2: Ввводим сумму
        amountField.sendKeys(depositAmount.toString());
        depositButton.shouldBe(visible).click();

        // ШАГ 3: проверка сообщения об ошибке
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please select an account.");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/deposit");

        //ШАГ 4: проверка суммы на UI и через API
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isZero();
    }

    @Test
    public void userCanNotDepositWithEmptyAmount() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        account = UserSteps.createAccount(user);
        String accountNumber = account.accountNumber();
        Double depositAmount = getDepositAmount();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 1: юзер кликает на Deposit Money
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);

        //ШАГ 2: Выбираем счет
        selectAccountField.click();
        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
        selectAccount.click();
        selectAccountField.shouldHave(text(accountNumber));

        //ШАГ 3: Кликаем на кнопку Deposit
        depositButton.shouldBe(visible).click();

        // ШАГ 4: проверка сообщения об ошибке
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please enter a valid amount.");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/deposit");

        //ШАГ 5: проверка суммы на UI и через API
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isZero();
    }

    @Test
    public void userCanNotDepositWithInvalidAmount() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        account = UserSteps.createAccount(user);
        String accountNumber = account.accountNumber();
        Double depositAmount = getDepositAmount();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 1: юзер кликает на Deposit Money
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);

        //ШАГ 2: Выбираем счет
        selectAccountField.click();
        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
        selectAccount.click();
        selectAccountField.shouldHave(text(accountNumber));

        //ШАГ 3: Вводим невалидную сумму и кликаем на кнопку Deposit
        amountField.sendKeys("-" + depositAmount);
        depositButton.shouldBe(visible).click();

        // ШАГ 4: проверка сообщения об ошибке
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Please enter a valid amount.");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/deposit");

        //ШАГ 5: проверка суммы на UI и через API
        depositMoneyButton.click();
        depositMoneyHeader.shouldBe(visible);

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isZero();
    }
}
