package nBankTests.ui.iteration_2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import generatos.RandomData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import utils.AccountData;
import utils.UserData;

import java.util.Map;

import static com.codeborne.selenide.Condition.clickable;
import static com.codeborne.selenide.Condition.partialText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;
import static generatos.RandomData.generateRandomAccountId;
import static generatos.RandomData.getDepositAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;


public class TransferTest {
    private static UserData user;
    private static AccountData account_1;
    private static AccountData account_2;

    public final SelenideElement makeTransferButton = $(byText("\uD83D\uDD04 Make a Transfer"));
    public final SelenideElement makeTransferHeader = $(byText("\uD83D\uDD04 Make a Transfer"));

    // Кнопки в тоггле
    public final SelenideElement newTransferButton = $(byText("\uD83C\uDD95 New Transfer"));
    public final SelenideElement transferAgainButton = $(byText("\uD83D\uDD01 Transfer Again"));

    // Выбор аккаунта
    public final SelenideElement selectAccountFieldTitle = $(byText("Select Your Account:"));
    public final SelenideElement selectAccountField = $(".account-selector");


    // Имя получателя
    public final SelenideElement recipientNameFieldTitle = $(byText("Recipient Name:"));
    public final SelenideElement recipientNameField = $(byAttribute("placeholder", "Enter recipient name"));

    // Номер счета получателя
    public final SelenideElement recipientAccountNumberFieldTitle = $(byText("Recipient Account Number:"));
    public final SelenideElement recipientAccountNumberField = $(byAttribute("placeholder", "Enter recipient account number"));

    // Сумма перевода
    public final SelenideElement amountFieldTitle = $(byText("Amount:"));
    public final SelenideElement amountField = $(byAttribute("placeholder", "Enter amount"));

    // Подтверждение деталей
    public final SelenideElement confirmDetailsLabel = $(byText("Confirm details are correct"));
    public final SelenideElement confirmDetailsCheckbox = $("#confirmCheck");

    // Кнопка отправки
    public final SelenideElement sendTransferButton = $(byText("\uD83D\uDE80 Send Transfer"));


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
        Double depositAmount = 5000.00;
        user = AdminSteps.createUser();
        account_1 = UserSteps.createAndDepositAccount(user, depositAmount, 1);
        account_2 = UserSteps.createAccount(user);

        String accountNumber_1 = account_1.accountNumber();
        String accountNumber_2 = account_2.accountNumber();
        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);
        String userName = RandomData.getUserName();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // юзер кликает на Make a Transfer
        makeTransferButton.click();

        //Проверка элементов на странице
        makeTransferHeader.shouldBe(visible);
        newTransferButton.shouldBe(visible).shouldBe(clickable);
        transferAgainButton.shouldBe(visible).shouldBe(clickable);

        //Выбираем счет
        selectAccountFieldTitle.shouldBe(visible);
        selectAccountField.shouldBe(visible).shouldHave(text("-- Choose an account --"));
        selectAccountField.click();

        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber_1));
        selectAccount.click();
        selectAccountField.shouldHave(text(account_1.accountNumber()));

        // Выбираем имя получателя
        recipientNameFieldTitle.shouldBe(visible);
        recipientNameField.shouldBe(visible).sendKeys(userName);

        // Выбираем номер счета получателя
        recipientAccountNumberFieldTitle.shouldBe(visible);
        recipientAccountNumberField.shouldBe(visible).sendKeys(accountNumber_2);

        //Проверка поля для ввода суммы, ввод суммы
        amountFieldTitle.shouldBe(visible);
        amountField.shouldBe(visible).sendKeys(transferAmount.toString());

        //Проверка корректности деталей операции
        confirmDetailsLabel.shouldBe(visible);
        confirmDetailsCheckbox.shouldBe(visible).click();
        sendTransferButton.shouldBe(visible).click();

        // Проверка сообщения об успешной операции
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully transferred $" + transferAmount + " to account " + accountNumber_2 + "!");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/transfer");

        //Проверка суммы на UI и через API
        Selenide.refresh();
        makeTransferHeader.shouldBe(visible);

        //счет 1
        Double accountBalanceAfter_1 = accountBalanceBeforeTransfer_1 - transferAmount;
        String expectedBalance = String.format("%.2f", accountBalanceAfter_1).replace(',', '.');
        selectAccount.shouldHave(partialText(expectedBalance));

        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isEqualTo(accountBalanceAfter_1, within(0.0001));

        //счет 2
        Double accountBalanceAfter_2 = accountBalanceBeforeTransfer_2 + transferAmount;
        SelenideElement receivedAccount = selectAccountField.$(byText(accountNumber_2));
        receivedAccount.click();
        receivedAccount.shouldHave(text(accountBalanceAfter_2.toString()));

        Double accountBalanceAfterTransfer_2 = UserSteps.getBalance(user, account_2);
        assertThat(accountBalanceAfterTransfer_2).isEqualTo(accountBalanceAfter_2, within(0.0001));
    }

    @Test
    public void userCanDepositWithEmptyRecipientNameTest() {
        //создаем тестовые данные
        Double depositAmount = 5000.00;
        user = AdminSteps.createUser();
        account_1 = UserSteps.createAndDepositAccount(user, depositAmount, 1);
        account_2 = UserSteps.createAccount(user);

        String accountNumber_1 = account_1.accountNumber();
        String accountNumber_2 = account_2.accountNumber();
        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // юзер кликает на Make a Transfer
        makeTransferButton.click();
        makeTransferHeader.shouldBe(visible);

        //Выбираем счет
        selectAccountField.click();

        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber_1));
        selectAccount.click();
        selectAccountField.shouldHave(text(account_1.accountNumber()));

        // Выбираем номер счета получателя
        recipientAccountNumberField.shouldBe(visible).sendKeys(accountNumber_2);

        //Ввод суммы
        amountField.shouldBe(visible).sendKeys(transferAmount.toString());

        //Проверка корректности деталей операции
        confirmDetailsLabel.shouldBe(visible);
        confirmDetailsCheckbox.shouldBe(visible).click();
        sendTransferButton.shouldBe(visible).click();

        // Проверка сообщения об успешной операции
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Successfully transferred $" + transferAmount + " to account " + accountNumber_2 + "!");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/transfer");

        //Проверка суммы на UI и через API
        Selenide.refresh();
        makeTransferHeader.shouldBe(visible);

        //счет 1
        Double accountBalanceAfter_1 = accountBalanceBeforeTransfer_1 - transferAmount;
        String expectedBalance = String.format("%.2f", accountBalanceAfter_1).replace(',', '.');
        selectAccount.shouldHave(partialText(expectedBalance));

        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isEqualTo(accountBalanceAfter_1, within(0.0001));

        //счет 2
        Double accountBalanceAfter_2 = accountBalanceBeforeTransfer_2 + transferAmount;
        SelenideElement receivedAccount = selectAccountField.$(byText(accountNumber_2));
        receivedAccount.click();
        receivedAccount.shouldHave(text(accountBalanceAfter_2.toString()));

        Double accountBalanceAfterTransfer_2 = UserSteps.getBalance(user, account_2);
        assertThat(accountBalanceAfterTransfer_2).isEqualTo(accountBalanceAfter_2, within(0.0001));
    }

    @Test
    public void userCanNotTransferOnNonExistentAccount() {
        //создаем тестовые данные
        Double depositAmount = 5000.00;
        user = AdminSteps.createUser();
        account_1 = UserSteps.createAndDepositAccount(user, depositAmount, 1);
        int receiverAccountId = generateRandomAccountId();
        String accountNumber_1 = account_1.accountNumber();
        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        String userName = "Noname";

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // юзер кликает на Make a Transfer
        makeTransferButton.click();

        //Выбираем счет
        selectAccountField.click();
        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber_1));
        selectAccount.click();
        selectAccountField.shouldHave(text(account_1.accountNumber()));

        // Выбираем имя получателя
        recipientNameField.shouldBe(visible).sendKeys(userName);

        // Выбираем номер счета получателя
        recipientAccountNumberField.shouldBe(visible).sendKeys(String.valueOf(receiverAccountId));

        //Ввод суммы
        amountField.shouldBe(visible).sendKeys(transferAmount.toString());

        //Проверка корректности деталей операции
        confirmDetailsCheckbox.shouldBe(visible).click();
        sendTransferButton.shouldBe(visible).click();

        // Проверка сообщения об успешной операции
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ No user found with this account number.");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/transfer");

        //ШАГ 5: проверка суммы счета отправителя на UI и через API
        Selenide.refresh();
        makeTransferHeader.shouldBe(visible);

        Double accountBalanceAfter_1 = accountBalanceBeforeTransfer_1;
        String expectedBalance = String.format("%.2f", accountBalanceAfter_1).replace(',', '.');
        selectAccount.shouldHave(partialText(expectedBalance));

        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isEqualTo(accountBalanceAfter_1, within(0.0001));
    }

    @Test
    public void userCanNotTransferWhenTransferAmountExceedsAccountBalance() {
        user = AdminSteps.createUser();
        account_1 = UserSteps.createAccount(user);
        account_2 = UserSteps.createAccount(user);

        String accountNumber_1 = account_1.accountNumber();
        String accountNumber_2 = account_2.accountNumber();
        Double transferAmount = getDepositAmount();
        String userName = RandomData.getUserName();
        String expectedBalance = "Balance: $0.00";


        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.authHeader());
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // юзер кликает на Make a Transfer
        makeTransferButton.click();
        makeTransferHeader.shouldBe(visible);

        //Выбираем счет
        selectAccountField.click();

        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber_1));
        selectAccount.click();
        selectAccountField.shouldHave(text(account_1.accountNumber()));

        // Выбираем имя получателя
        recipientNameField.shouldBe(visible).sendKeys(userName);

        // Выбираем номер счета получателя
        recipientAccountNumberField.shouldBe(visible).sendKeys(accountNumber_2);

        //Проверка поля для ввода суммы, ввод суммы
        amountField.shouldBe(visible).sendKeys(transferAmount.toString());

        //Проверка корректности деталей операции
        confirmDetailsLabel.shouldBe(visible);
        confirmDetailsCheckbox.shouldBe(visible).click();
        sendTransferButton.shouldBe(visible).click();

        // Проверка сообщения об успешной операции
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");
        alert.accept();
        assertThat(WebDriverRunner.url()).contains("/transfer");

        //Проверка суммы на UI и через API
        Selenide.refresh();
        makeTransferHeader.shouldBe(visible);

        //счет 1
        selectAccount.shouldHave(partialText(expectedBalance));
        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isZero();

        //счет 2
        SelenideElement receivedAccount = selectAccountField.$(byText(accountNumber_2));
        receivedAccount.click();
        receivedAccount.shouldHave(text(expectedBalance));

        Double accountBalanceAfterTransfer_2 = UserSteps.getBalance(user, account_2);
        assertThat(accountBalanceAfterTransfer_2).isZero();
    }
}
