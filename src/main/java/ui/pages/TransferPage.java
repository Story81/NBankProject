package ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import common.helpers.StepLogger;
import lombok.Getter;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

@Getter
public class TransferPage extends BasePage<TransferPage> {

    private SelenideElement makeTransferHeader = $(byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement newTransferButton = $(byText("\uD83C\uDD95 New Transfer"));
    private SelenideElement transferAgainButton = $(byText("\uD83D\uDD01 Transfer Again"));

    // Выбор аккаунта
    private SelenideElement selectAccountFieldTitle = $(byText("Select Your Account:"));
    private SelenideElement selectAccountField = $(".account-selector");

    // Имя получателя
    private SelenideElement recipientNameFieldTitle = $(byText("Recipient Name:"));
    private SelenideElement recipientNameField = $(byAttribute("placeholder", "Enter recipient name"));

    // Номер счета получателя
    private SelenideElement recipientAccountNumberFieldTitle = $(byText("Recipient Account Number:"));
    private SelenideElement recipientAccountNumberField = $(byAttribute("placeholder", "Enter recipient account number"));

    // Сумма перевода
    private SelenideElement amountFieldTitle = $(byText("Amount:"));
    private SelenideElement amountField = $(byAttribute("placeholder", "Enter amount"));

    // Подтверждение деталей
    private SelenideElement confirmDetailsLabel = $(byText("Confirm details are correct"));
    private SelenideElement confirmDetailsCheckbox = $("#confirmCheck");

    // Кнопка отправки
    private SelenideElement sendTransferButton = $(byText("\uD83D\uDE80 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage checkTransferHeader() {
        return StepLogger.log("Check header of transfer page", () -> {
            assertThat(WebDriverRunner.url()).contains(url());
            makeTransferHeader.shouldBe(visible);
            return this;
        });
    }

    public TransferPage checkSelectAccountField() {
        return StepLogger.log(String.format("Click on sender account field"), () -> {
            selectAccountFieldTitle.shouldBe(visible);
            selectAccountField.shouldBe(visible).shouldHave(text("-- Choose an account --"));
            selectAccountField.click();
            return this;
        });
    }

    public TransferPage selectSenderAccount(String accountNumber) {
        return StepLogger.log(String.format("Select sender account: %s", accountNumber), () -> {
            SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
            selectAccount.click();
            selectAccountField.shouldHave(text(accountNumber));
            return this;
        });
    }

    public TransferPage inputRecipientName(String userName) {
        return StepLogger.log(String.format("Input recipient name: %s", userName), () -> {
            recipientNameFieldTitle.shouldBe(visible);
            recipientNameField.shouldBe(visible).sendKeys(userName);
            return this;
        });
    }

    public TransferPage selectRecipientAccount(String accountNumber) {
        return StepLogger.log(String.format("Select recipient account: %s", accountNumber), () -> {
            recipientAccountNumberFieldTitle.shouldBe(visible);
            recipientAccountNumberField.shouldBe(visible).sendKeys(accountNumber);
            return this;
        });
    }

    public TransferPage inputTransferAmount(Double transferAmount) {
        return StepLogger.log(String.format("Input transfer amount: %.2f", transferAmount), () -> {
            amountFieldTitle.shouldBe(visible);
            amountField.shouldBe(visible).sendKeys(transferAmount.toString());
            return this;
        });
    }

    public TransferPage clickConfirmDetailsCheckbox() {
        return StepLogger.log("Click confirm details checkbox", () -> {
            confirmDetailsLabel.shouldBe(visible);
            confirmDetailsCheckbox.shouldBe(visible).click();
            return this;
        });
    }

    public TransferPage clickTransferButton() {
        return StepLogger.logWithScreenshotBefore("Click transfer button", () -> {
            sendTransferButton.shouldBe(visible).click();
            return this;
        });
    }

    public TransferPage checkSenderAccountBalance(String accountNumber, Double balance) {
        return StepLogger.log(String.format("Check sender account %s balance: %.2f", accountNumber, balance), () -> {
            SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
            String expectedBalance = String.format("%.2f", balance).replace(',', '.');
            selectAccount.shouldHave(text(expectedBalance));
            return this;
        });
    }

    public TransferPage checkReceivedAccountBalance(String accountNumber, Double balance) {
        return StepLogger.log(String.format("Check received account %s balance: %.2f", accountNumber, balance), () -> {
            SelenideElement receivedAccount = selectAccountField.$(byText(accountNumber));
            receivedAccount.click();
            receivedAccount.shouldHave(text(balance.toString()));
            return this;
        });
    }
}